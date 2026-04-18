package com.jackyblackson.idunntemplates.command.sub.project;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.command.sub.BaseSubCommand;
import com.jackyblackson.idunntemplates.core.api.BackendApiClient;
import com.jackyblackson.idunntemplates.manager.ProjectCatalogManager;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateProjectCommand extends BaseSubCommand {

    private final BackendApiClient backendApiClient;
    private final ProjectCatalogManager projectCatalogManager;

    public CreateProjectCommand(BackendApiClient backendApiClient, ProjectCatalogManager projectCatalogManager) {
        this.backendApiClient = backendApiClient;
        this.projectCatalogManager = projectCatalogManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 5) {
            sendUsage(player);
            return;
        }

        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
        Region region;
        try {
            region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException e) {
            player.sendMessage(ChatColor.RED + "You need an active WorldEdit cuboid selection first.");
            return;
        }

        if (!(region instanceof CuboidRegion)) {
            player.sendMessage(ChatColor.RED + "Only cuboid selections are supported for in-game project creation.");
            return;
        }

        BackendApiClient.ProjectCreateRequest request;
        try {
            request = buildRequest(player, region, args);
        } catch (IllegalArgumentException ex) {
            player.sendMessage(ChatColor.RED + ex.getMessage());
            sendUsage(player);
            return;
        }
        player.sendMessage(ChatColor.YELLOW + "Creating project from your current selection...");

        backendApiClient.createInGameProject(request).thenAccept(response ->
                IdunnTemplates.getInstance().getServer().getScheduler().runTask(IdunnTemplates.getInstance(), () -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    if (response.isSuccess() && response.getProject() != null) {
                        player.sendMessage(ChatColor.GREEN + "Project created successfully: "
                                + response.getProject().displayName
                                + ChatColor.GRAY + " (#" + response.getProject().id + ")");
                        BackendApiClient.ProjectDetails details = new BackendApiClient.ProjectDetails();
                        details.id = response.getProject().id;
                        details.name = response.getProject().name;
                        details.displayName = response.getProject().displayName;
                        details.pathName = response.getProject().pathName;
                        details.kind = response.getProject().kind;
                        details.worldId = response.getProject().worldId;
                        details.worldName = request.worldName;
                        details.minX = request.minX;
                        details.minY = request.minY;
                        details.minZ = request.minZ;
                        details.maxX = request.maxX;
                        details.maxY = request.maxY;
                        details.maxZ = request.maxZ;
                        projectCatalogManager.upsertProject(details);
                        IdunnTemplates.getInstance().getProjectSettlementSyncManager().refreshProject(response.getProject().id);
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to create project: "
                                + (response.getErrorMessage() != null ? response.getErrorMessage() : "unknown error"));
                    }
                })
        );
    }

    private BackendApiClient.ProjectCreateRequest buildRequest(Player player, Region region, String[] args) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        Location location = player.getLocation();
        Map<String, String> options = parseOptions(args);

        BackendApiClient.ProjectCreateRequest request = new BackendApiClient.ProjectCreateRequest();
        request.name = args[1];
        request.displayName = args[2];
        request.pathName = normalizePath(args[3]);
        request.kind = args[4];
        request.description = emptyToNull(options.get("desc"));
        request.modelKind = emptyToNull(firstNonBlank(options, "model", "modelkind"));
        request.worldName = emptyToNull(options.getOrDefault("world", player.getWorld().getName()));
        request.minX = min.x();
        request.minY = min.y();
        request.minZ = min.z();
        request.maxX = max.x();
        request.maxY = max.y();
        request.maxZ = max.z();
        request.tpX = parseDouble(options.get("tpx"), location.getX(), "tpx");
        request.tpY = parseDouble(options.get("tpy"), location.getY(), "tpy");
        request.tpZ = parseDouble(options.get("tpz"), location.getZ(), "tpz");
        request.tpYaw = parseDouble(options.get("yaw"), (double) location.getYaw(), "yaw");
        request.tpPitch = parseDouble(options.get("pitch"), (double) location.getPitch(), "pitch");
        request.parentProjectId = parseLong(options.get("parent"), null, "parent");
        request.creatorUsername = player.getName();
        request.sourceServerName = emptyToNull(options.getOrDefault("server", IdunnTemplates.getInstance().getServer().getName()));
        return request;
    }

    private Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 5; i < args.length; i++) {
            String arg = args[i];
            int splitIndex = arg.indexOf('=');
            if (splitIndex <= 0 || splitIndex == arg.length() - 1) {
                throw new IllegalArgumentException("Optional arguments must use key=value format. Invalid argument: " + arg);
            }
            String key = arg.substring(0, splitIndex).trim().toLowerCase(Locale.ROOT);
            String value = arg.substring(splitIndex + 1).trim();
            if (value.isEmpty()) {
                throw new IllegalArgumentException("Optional argument value cannot be empty: " + arg);
            }
            options.put(key, value);
        }
        return options;
    }

    private String firstNonBlank(Map<String, String> options, String... keys) {
        for (String key : keys) {
            String value = options.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Double parseDouble(String raw, Double fallback, String fieldName) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid numeric value for " + fieldName + ": " + raw);
        }
    }

    private Long parseLong(String raw, Long fallback, String fieldName) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid numeric value for " + fieldName + ": " + raw);
        }
    }

    private String normalizePath(String path) {
        String normalized = path.replace('\\', '/').replaceAll("/{2,}", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /idunn project create <name> <display> <path> <type> [key=value ...]");
        player.sendMessage(ChatColor.GRAY + "Optional keys: desc=..., model=..., parent=123, world=world_nether, tpx=..., tpy=..., tpz=..., yaw=..., pitch=...");
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return List.of("<name>");
        }
        if (args.length == 3) {
            return List.of("<display>");
        }
        if (args.length == 4) {
            return List.of("<path>");
        }
        if (args.length == 5) {
            return List.of("<type>");
        }
        if (args.length >= 6) {
            return filter(
                    List.of(
                            "desc=",
                            "model=",
                            "parent=",
                            "world=" + player.getWorld().getName(),
                            "tpx=",
                            "tpy=",
                            "tpz=",
                            "yaw=",
                            "pitch="
                    ),
                    args[args.length - 1]
            );
        }
        return Collections.emptyList();
    }
}
