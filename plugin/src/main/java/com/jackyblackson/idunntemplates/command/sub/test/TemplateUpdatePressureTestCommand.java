package com.jackyblackson.idunntemplates.command.sub.test;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.store.InstanceRepository;
import com.jackyblackson.idunntemplates.core.util.TransformUtil;
import com.jackyblackson.idunntemplates.manager.TemplateManager;
import com.jackyblackson.idunntemplates.util.EntityHelper;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class TemplateUpdatePressureTestCommand extends BaseSubCommand {

    private static final String PERMISSION = "idunn.test.update";
    private static final int CHUNK_RADIUS = 20;
    private static final int INSTANCES_PER_CHUNK = 20;
    private static final int CHUNKS_PER_TICK = 2;
    private static final int TELEPORT_INTERVAL_TICKS = 1;
    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT).withZone(ZoneId.systemDefault());

    private final TemplateManager templateManager;
    private final InstanceRepository instanceRepository;

    public TemplateUpdatePressureTestCommand(TemplateManager templateManager, InstanceRepository instanceRepository) {
        this.templateManager = templateManager;
        this.instanceRepository = instanceRepository;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to run this test.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /idunn test update <templatePath>");
            return;
        }

        String templatePath = args[1];
        Template template = templateManager.getTemplate(templatePath);
        if (template == null) {
            player.sendMessage(ChatColor.RED + "Template not found: " + templatePath);
            return;
        }

        TemplateVersion latest = template.getLatestVersion();
        if (latest == null) {
            player.sendMessage(ChatColor.RED + "Template has no version to place: " + templatePath);
            return;
        }

        World world = player.getWorld();
        TemplateMetadata metadata = template.getMetadata();
        World sourceWorld = Bukkit.getWorld(metadata.getWorldId());
        if (sourceWorld == null) {
            player.sendMessage(ChatColor.RED + "The template source world is not loaded, so the test cannot commit updates.");
            return;
        }
        if (!Objects.equals(metadata.getWorldId(), world.getUID())) {
            player.sendMessage(ChatColor.YELLOW + "The template source world is different from your current world.");
            player.sendMessage(ChatColor.YELLOW + "The test will still run in " + world.getName()
                    + ", but commits will capture from " + sourceWorld.getName() + ".");
        }

        Clipboard clipboard = EntityHelper.getClipboard(template, latest.getVersionId());
        if (clipboard == null) {
            player.sendMessage(ChatColor.RED + "Failed to load template schematic for the latest version.");
            return;
        }

        int width = metadata.getWidth();
        int height = metadata.getHeight();
        int length = metadata.getLength();
        if (width != 10 || height != 10 || length != 10) {
            player.sendMessage(ChatColor.YELLOW + "Warning: PT-2 baseline assumes 10x10x10, but this template is "
                    + width + "x" + height + "x" + length + ".");
        }

        TestRunContext context = buildContext(player, template, clipboard);
        if (context.baseY < world.getMinHeight()) {
            player.sendMessage(ChatColor.RED + "Unable to find a safe vertical range for "
                    + INSTANCES_PER_CHUNK + " stacked placements in the current world height.");
            return;
        }

        player.sendMessage(ChatColor.AQUA + "[PT-2] Starting template update pressure test...");
        player.sendMessage(ChatColor.GRAY + "Template: " + ChatColor.WHITE + template.getPath());
        player.sendMessage(ChatColor.GRAY + "Chunk radius: " + ChatColor.WHITE + CHUNK_RADIUS
                + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "Density: " + ChatColor.WHITE + INSTANCES_PER_CHUNK + "/chunk");
        player.sendMessage(ChatColor.GRAY + "Target chunks: " + ChatColor.WHITE + context.targetChunks.size()
                + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "Planned instances: " + ChatColor.WHITE + context.totalPlannedInstances);

        startPlacementPhase(player, context);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return filter(templateManager.getNextPathsFor(args[1]), args[1]);
        }
        return List.of();
    }

    private TestRunContext buildContext(Player player, Template template, Clipboard clipboard) {
        Chunk centerChunk = player.getLocation().getChunk();
        World world = player.getWorld();
        List<ChunkCoord> targetChunks = new ArrayList<>();
        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                if (dx * dx + dz * dz > CHUNK_RADIUS * CHUNK_RADIUS) {
                    continue;
                }
                targetChunks.add(new ChunkCoord(centerChunk.getX() + dx, centerChunk.getZ() + dz));
            }
        }
        targetChunks.sort(Comparator
                .comparingInt((ChunkCoord chunk) -> Math.abs(chunk.x - centerChunk.getX()) + Math.abs(chunk.z - centerChunk.getZ()))
                .thenComparingInt(chunk -> chunk.x)
                .thenComparingInt(chunk -> chunk.z));

        int spacingY = template.getMetadata().getHeight() + 2;
        int maxBaseY = world.getMaxHeight() - 2 - ((INSTANCES_PER_CHUNK - 1) * spacingY + template.getMetadata().getHeight());
        int minBaseY = world.getMinHeight() + 1;
        int baseY = Math.min(Math.max(player.getLocation().getBlockY(), minBaseY), maxBaseY);
        if (maxBaseY < minBaseY) {
            baseY = world.getMinHeight() - 1;
        }

        String runId = FILE_TIME.format(Instant.now()) + "-" + player.getName().toLowerCase(Locale.ROOT);
        File outputDir = new File(IdunnTemplates.getInstance().getDataFolder(), "test-results");
        File outputFile = new File(outputDir, "template-update-pressure-" + runId + ".csv");
        return new TestRunContext(player.getUniqueId(), template, clipboard, world, centerChunk.getX(), centerChunk.getZ(),
                baseY, spacingY, targetChunks, outputFile);
    }

    private void startPlacementPhase(Player player, TestRunContext context) {
        long startedAt = System.currentTimeMillis();
        AtomicInteger chunkIndex = new AtomicInteger(0);
        List<CompletableFuture<Void>> saveFutures = new ArrayList<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                int processedChunks = 0;
                while (processedChunks < CHUNKS_PER_TICK && chunkIndex.get() < context.targetChunks.size()) {
                    ChunkCoord chunk = context.targetChunks.get(chunkIndex.getAndIncrement());
                    processedChunks++;
                    try {
                        placeStackInChunk(player, context, chunk, saveFutures);
                    } catch (Exception ex) {
                        cancel();
                        player.sendMessage(ChatColor.RED + "PT-2 placement failed: " + ex.getMessage());
                        ex.printStackTrace();
                        return;
                    }
                }

                if (chunkIndex.get() >= context.targetChunks.size()) {
                    cancel();
                    context.placementDurationMs = System.currentTimeMillis() - startedAt;
                    player.sendMessage(ChatColor.GREEN + "[PT-2] Placement finished in "
                            + context.placementDurationMs + " ms. Waiting for instance records to persist...");

                    CompletableFuture
                            .allOf(saveFutures.toArray(CompletableFuture[]::new))
                            .whenComplete((unused, throwable) -> IdunnTemplates.getInstance().getServer().getScheduler()
                                    .runTask(IdunnTemplates.getInstance(), () -> {
                                        if (throwable != null) {
                                            player.sendMessage(ChatColor.RED + "PT-2 record persistence failed: " + throwable.getMessage());
                                            throwable.printStackTrace();
                                            return;
                                        }
                                        startMutationAndCommitPhase(player, context);
                                    }));
                    return;
                }

                if (chunkIndex.get() % 50 == 0 || chunkIndex.get() == context.targetChunks.size()) {
                    player.sendMessage(ChatColor.GRAY + "[PT-2] Placement progress: "
                            + chunkIndex.get() + "/" + context.targetChunks.size() + " chunks, "
                            + context.placedInstances + "/" + context.totalPlannedInstances + " instances");
                }
            }
        }.runTaskTimer(IdunnTemplates.getInstance(), 1L, 1L);
    }

    private void placeStackInChunk(Player player, TestRunContext context, ChunkCoord chunk, List<CompletableFuture<Void>> saveFutures) throws Exception {
        World world = context.world;
        world.getChunkAt(chunk.x, chunk.z).load();
        int anchorX = chunk.x * 16 + 3;
        int anchorZ = chunk.z * 16 + 3;

        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(BukkitAdapter.adapt(world))
                .build()) {
            for (int layer = 0; layer < INSTANCES_PER_CHUNK; layer++) {
                int y = context.baseY + layer * context.verticalSpacing;
                Location location = new Location(world, anchorX, y, anchorZ);
                pasteClipboard(session, context.clipboard, location);

                BlockVector3 minPos = TransformUtil.getInstanceMinPos(location, context.clipboard);
                Instance instance = new Instance(
                        context.template.getId(),
                        context.template.getLatestVersion().getVersionId(),
                        world.getUID(),
                        minPos.x(), minPos.y(), minPos.z(),
                        0, false, false, false,
                        context.ownerId,
                        player.getName()
                );
                saveFutures.add(instanceRepository.saveInstance(instance));
                context.placedInstances++;
            }
        }
    }

    private void pasteClipboard(EditSession session, Clipboard clipboard, Location location) throws Exception {
        ClipboardHolder holder = new ClipboardHolder(clipboard);
        Operation op = holder.createPaste(session)
                .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                .ignoreAirBlocks(true)
                .build();
        Operations.completeLegacy(op);
    }

    private void startMutationAndCommitPhase(Player player, TestRunContext context) {
        long mutateStarted = System.currentTimeMillis();
        try {
            applyTemplateMutation(context.template);
            context.mutationDurationMs = System.currentTimeMillis() - mutateStarted;
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "PT-2 template mutation failed: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "[PT-2] Template source mutated in " + context.mutationDurationMs + " ms. Committing...");

        long commitStarted = System.currentTimeMillis();
        try {
            templateManager.commitTemplateSystem(context.template, "[PT-2] automatic pressure update " + FILE_TIME.format(Instant.now()));
            context.commitDurationMs = System.currentTimeMillis() - commitStarted;
            TemplateVersion latest = context.template.getLatestVersion();
            context.newVersionId = latest != null ? latest.getVersionId() : null;
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "PT-2 commit failed: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        player.sendMessage(ChatColor.GREEN + "[PT-2] Commit and immediate update finished in "
                + context.commitDurationMs + " ms. Starting movement sampling...");
        startMovementPhase(player, context);
    }

    private void applyTemplateMutation(Template template) {
        TemplateMetadata meta = template.getMetadata();
        World world = Bukkit.getWorld(meta.getWorldId());
        if (world == null) {
            throw new IllegalStateException("Template source world is not loaded.");
        }

        Material primary = (template.getMetadata().getVersions().size() % 2 == 0) ? Material.SEA_LANTERN : Material.STONE_BRICKS;
        Material secondary = (template.getMetadata().getVersions().size() % 2 == 0) ? Material.POLISHED_ANDESITE : Material.DEEPSLATE_TILES;

        for (int x = 0; x < meta.getWidth(); x++) {
            for (int y = 0; y < meta.getHeight(); y++) {
                for (int z = 0; z < meta.getLength(); z++) {
                    Material material = ((x + y + z) % 2 == 0) ? primary : secondary;
                    world.getBlockAt(meta.getAnchorX() + x, meta.getAnchorY() + y, meta.getAnchorZ() + z).setType(material, false);
                }
            }
        }
    }

    private void startMovementPhase(Player player, TestRunContext context) {
        long movementStarted = System.currentTimeMillis();
        AtomicInteger index = new AtomicInteger(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    finishRun(player, context, movementStarted);
                    cancel();
                    return;
                }
                if (index.get() >= context.targetChunks.size()) {
                    finishRun(player, context, movementStarted);
                    cancel();
                    return;
                }

                ChunkCoord target = context.targetChunks.get(index.getAndIncrement());
                Chunk chunk = context.world.getChunkAt(target.x, target.z);
                chunk.load();
                Location destination = new Location(
                        context.world,
                        target.x * 16 + 8.5,
                        context.baseY + 1.0,
                        target.z * 16 + 8.5,
                        player.getLocation().getYaw(),
                        player.getLocation().getPitch()
                );
                player.teleport(destination);

                SampleRow sample = buildSampleRow(context, movementStarted, target, index.get());
                context.samples.add(sample);

                if (sample.staleLoadedInstances == 0 && sample.loadedTemplateInstances > 0 && context.firstLoadedClearMs == null) {
                    context.firstLoadedClearMs = sample.elapsedMs;
                }

                if (index.get() % 100 == 0 || index.get() == context.targetChunks.size()) {
                    player.sendMessage(ChatColor.GRAY + "[PT-2] Movement progress: "
                            + index.get() + "/" + context.targetChunks.size()
                            + " chunks, TPS=" + formatDouble(sample.tps1m)
                            + ", MSPT=" + formatDouble(sample.mspt)
                            + ", staleLoaded=" + sample.staleLoadedInstances);
                }
            }
        }.runTaskTimer(IdunnTemplates.getInstance(), 1L, TELEPORT_INTERVAL_TICKS);
    }

    private SampleRow buildSampleRow(TestRunContext context, long movementStarted, ChunkCoord chunk, int visitedChunks) {
        long now = System.currentTimeMillis();
        double[] tps = readTps();
        double mspt = readMspt();

        int loaded = 0;
        int stale = 0;
        String latestVersion = context.newVersionId != null ? context.newVersionId :
                (context.template.getLatestVersion() != null ? context.template.getLatestVersion().getVersionId() : null);

        for (Instance instance : instanceRepository.getAllLoadedInstances()) {
            if (!context.template.getId().equals(instance.getTemplateId())) {
                continue;
            }
            int chunkDx = (instance.getX() >> 4) - context.centerChunkX;
            int chunkDz = (instance.getZ() >> 4) - context.centerChunkZ;
            if (chunkDx * chunkDx + chunkDz * chunkDz > CHUNK_RADIUS * CHUNK_RADIUS) {
                continue;
            }
            loaded++;
            if (latestVersion != null && !latestVersion.equals(instance.getCurrentVersionId())) {
                stale++;
            }
        }

        return new SampleRow(
                now,
                now - movementStarted,
                chunk.x,
                chunk.z,
                visitedChunks,
                loaded,
                stale,
                tps[0],
                tps[1],
                tps[2],
                mspt
        );
    }

    private void finishRun(Player player, TestRunContext context, long movementStarted) {
        context.movementDurationMs = System.currentTimeMillis() - movementStarted;
        writeCsvAsync(player, context);
    }

    private void writeCsvAsync(Player player, TestRunContext context) {
        CompletableFuture.runAsync(() -> {
            File parent = context.outputFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(context.outputFile))) {
                writer.println("rowType,runId,templatePath,templateId,versionId,timestampMs,elapsedMs,chunkX,chunkZ,visitedChunks,loadedTemplateInstances,staleLoadedInstances,tps1m,tps5m,tps15m,mspt,placementDurationMs,mutationDurationMs,commitDurationMs,movementDurationMs,totalPlannedInstances,placedInstances,targetChunks,firstLoadedClearMs");
                writer.printf(Locale.ROOT,
                        "summary,%s,%s,%s,%s,,,,,,,,,,,%d,%d,%d,%d,%d,%d,%d,%s%n",
                        escape(context.runId),
                        escape(context.template.getPath()),
                        context.template.getId(),
                        escape(context.newVersionId),
                        context.placementDurationMs,
                        context.mutationDurationMs,
                        context.commitDurationMs,
                        context.movementDurationMs,
                        context.totalPlannedInstances,
                        context.placedInstances,
                        context.targetChunks.size(),
                        context.firstLoadedClearMs == null ? "" : context.firstLoadedClearMs.toString()
                );
                for (SampleRow sample : context.samples) {
                    writer.printf(Locale.ROOT,
                            "sample,%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%d,%.3f,%.3f,%.3f,%.3f,%d,%d,%d,%d,%d,%d,%d,%s%n",
                            escape(context.runId),
                            escape(context.template.getPath()),
                            context.template.getId(),
                            escape(context.newVersionId),
                            sample.timestampMs,
                            sample.elapsedMs,
                            sample.chunkX,
                            sample.chunkZ,
                            sample.visitedChunks,
                            sample.loadedTemplateInstances,
                            sample.staleLoadedInstances,
                            sample.tps1m,
                            sample.tps5m,
                            sample.tps15m,
                            sample.mspt,
                            context.placementDurationMs,
                            context.mutationDurationMs,
                            context.commitDurationMs,
                            context.movementDurationMs,
                            context.totalPlannedInstances,
                            context.placedInstances,
                            context.targetChunks.size(),
                            context.firstLoadedClearMs == null ? "" : context.firstLoadedClearMs.toString()
                    );
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }).whenComplete((unused, throwable) -> IdunnTemplates.getInstance().getServer().getScheduler()
                .runTask(IdunnTemplates.getInstance(), () -> {
                    if (throwable != null) {
                        player.sendMessage(ChatColor.RED + "PT-2 CSV export failed: " + throwable.getMessage());
                        throwable.printStackTrace();
                        return;
                    }
                    double minTps = context.samples.stream().mapToDouble(sample -> sample.tps1m).min().orElse(readTps()[0]);
                    double maxMspt = context.samples.stream().mapToDouble(sample -> sample.mspt).max().orElse(readMspt());
                    player.sendMessage(ChatColor.GREEN + "[PT-2] Test completed.");
                    player.sendMessage(ChatColor.GRAY + "Commit/update time: " + ChatColor.WHITE + context.commitDurationMs + " ms");
                    player.sendMessage(ChatColor.GRAY + "Min TPS(1m): " + ChatColor.WHITE + formatDouble(minTps)
                            + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "Max MSPT: " + ChatColor.WHITE + formatDouble(maxMspt));
                    player.sendMessage(ChatColor.GRAY + "CSV: " + ChatColor.WHITE + context.outputFile.getAbsolutePath());
                    if (context.firstLoadedClearMs != null) {
                        player.sendMessage(ChatColor.GRAY + "First sample with staleLoaded=0: " + ChatColor.WHITE + context.firstLoadedClearMs + " ms");
                    }
                }));
    }

    private double[] readTps() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getTPS");
            Object result = method.invoke(Bukkit.getServer());
            if (result instanceof double[] values && values.length >= 3) {
                return values;
            }
        } catch (Exception ignored) {
        }
        return new double[] { Double.NaN, Double.NaN, Double.NaN };
    }

    private double readMspt() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getAverageTickTime");
            Object result = method.invoke(Bukkit.getServer());
            if (result instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Exception ignored) {
        }
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getAverageTickMillis");
            Object result = method.invoke(Bukkit.getServer());
            if (result instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Exception ignored) {
        }
        return Double.NaN;
    }

    private String formatDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "N/A";
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static final class ChunkCoord {
        private final int x;
        private final int z;

        private ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    private static final class SampleRow {
        private final long timestampMs;
        private final long elapsedMs;
        private final int chunkX;
        private final int chunkZ;
        private final int visitedChunks;
        private final int loadedTemplateInstances;
        private final int staleLoadedInstances;
        private final double tps1m;
        private final double tps5m;
        private final double tps15m;
        private final double mspt;

        private SampleRow(
                long timestampMs,
                long elapsedMs,
                int chunkX,
                int chunkZ,
                int visitedChunks,
                int loadedTemplateInstances,
                int staleLoadedInstances,
                double tps1m,
                double tps5m,
                double tps15m,
                double mspt
        ) {
            this.timestampMs = timestampMs;
            this.elapsedMs = elapsedMs;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.visitedChunks = visitedChunks;
            this.loadedTemplateInstances = loadedTemplateInstances;
            this.staleLoadedInstances = staleLoadedInstances;
            this.tps1m = tps1m;
            this.tps5m = tps5m;
            this.tps15m = tps15m;
            this.mspt = mspt;
        }
    }

    private static final class TestRunContext {
        private final UUID ownerId;
        private final Template template;
        private final Clipboard clipboard;
        private final World world;
        private final int centerChunkX;
        private final int centerChunkZ;
        private final int baseY;
        private final int verticalSpacing;
        private final List<ChunkCoord> targetChunks;
        private final int totalPlannedInstances;
        private final File outputFile;
        private final String runId;
        private final List<SampleRow> samples = new ArrayList<>();

        private int placedInstances = 0;
        private long placementDurationMs = 0L;
        private long mutationDurationMs = 0L;
        private long commitDurationMs = 0L;
        private long movementDurationMs = 0L;
        private String newVersionId;
        private Long firstLoadedClearMs;

        private TestRunContext(
                UUID ownerId,
                Template template,
                Clipboard clipboard,
                World world,
                int centerChunkX,
                int centerChunkZ,
                int baseY,
                int verticalSpacing,
                List<ChunkCoord> targetChunks,
                File outputFile
        ) {
            this.ownerId = ownerId;
            this.template = template;
            this.clipboard = clipboard;
            this.world = world;
            this.centerChunkX = centerChunkX;
            this.centerChunkZ = centerChunkZ;
            this.baseY = baseY;
            this.verticalSpacing = verticalSpacing;
            this.targetChunks = targetChunks;
            this.totalPlannedInstances = targetChunks.size() * INSTANCES_PER_CHUNK;
            this.outputFile = outputFile;
            this.runId = outputFile.getName().replace(".csv", "");
        }
    }
}
