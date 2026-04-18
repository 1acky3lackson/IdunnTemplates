package com.jackyblackson.idunntemplates.manager;

import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.api.BackendApiClient;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class ProjectCatalogManager {

    private static final long REFRESH_INTERVAL_MS = 60_000L;

    private final IdunnTemplates plugin;
    private final BackendApiClient backendApiClient;
    private final Logger logger;

    private volatile List<BackendApiClient.ProjectDetails> cachedProjects = List.of();
    private volatile long lastRefreshMs = 0L;
    private final AtomicReference<CompletableFuture<List<BackendApiClient.ProjectDetails>>> inFlightRefresh = new AtomicReference<>();
    private BukkitTask refreshTask;

    public ProjectCatalogManager(IdunnTemplates plugin, BackendApiClient backendApiClient, Logger logger) {
        this.plugin = plugin;
        this.backendApiClient = backendApiClient;
        this.logger = logger;
    }

    public void start() {
        refreshAsync(true);
        refreshTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                () -> refreshAsync(false),
                20L * 30L,
                20L * 60L
        );
    }

    public void stop() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    public CompletableFuture<List<BackendApiClient.ProjectDetails>> ensureFreshCatalog() {
        return refreshAsync(false);
    }

    public CompletableFuture<List<BackendApiClient.ProjectDetails>> refreshAsync(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && !cachedProjects.isEmpty() && now - lastRefreshMs < REFRESH_INTERVAL_MS) {
            return CompletableFuture.completedFuture(cachedProjects);
        }

        CompletableFuture<List<BackendApiClient.ProjectDetails>> running = inFlightRefresh.get();
        if (running != null && !running.isDone()) {
            return running;
        }

        CompletableFuture<List<BackendApiClient.ProjectDetails>> future = backendApiClient.listAllProjects()
                .thenApply(projects -> {
                    List<BackendApiClient.ProjectDetails> normalized = projects == null ? List.of() : projects.stream()
                            .filter(Objects::nonNull)
                            .filter(project -> project.deleteTimeMs == null)
                            .sorted(Comparator.comparing((BackendApiClient.ProjectDetails project) -> project.id == null ? -1L : project.id).reversed())
                            .toList();
                    cachedProjects = normalized;
                    lastRefreshMs = System.currentTimeMillis();
                    return normalized;
                })
                .exceptionally(ex -> {
                    logger.warning("Failed to refresh project catalog: " + ex.getMessage());
                    return cachedProjects;
                });

        inFlightRefresh.set(future);
        future.whenComplete((ignored, throwable) -> inFlightRefresh.compareAndSet(future, null));
        return future;
    }

    public void upsertProject(BackendApiClient.ProjectDetails project) {
        if (project == null || project.id == null) {
            return;
        }
        List<BackendApiClient.ProjectDetails> next = new ArrayList<>(cachedProjects);
        next.removeIf(existing -> Objects.equals(existing.id, project.id));
        next.add(project);
        next.sort(Comparator.comparing((BackendApiClient.ProjectDetails value) -> value.id == null ? -1L : value.id).reversed());
        cachedProjects = List.copyOf(next);
        lastRefreshMs = System.currentTimeMillis();
    }

    public List<BackendApiClient.ProjectDetails> getCachedProjects() {
        return cachedProjects;
    }

    public List<BackendApiClient.ProjectDetails> searchProjects(String rawQuery, int limit) {
        String query = normalize(rawQuery);
        if (query.isEmpty()) {
            return cachedProjects.stream().limit(limit).toList();
        }
        return cachedProjects.stream()
                .filter(project -> matches(project, query))
                .sorted(Comparator
                        .comparing((BackendApiClient.ProjectDetails project) -> exactMatchScore(project, query))
                        .thenComparing(project -> containsScore(project, query))
                        .thenComparing(project -> project.id == null ? Long.MAX_VALUE : -project.id))
                .limit(limit)
                .toList();
    }

    public Optional<BackendApiClient.ProjectDetails> findExactProject(String rawIdentifier) {
        Long parsedId = parseProjectId(rawIdentifier);
        if (parsedId != null) {
            return cachedProjects.stream()
                    .filter(project -> Objects.equals(project.id, parsedId))
                    .findFirst();
        }

        String query = normalize(rawIdentifier);
        if (query.isEmpty()) {
            return Optional.empty();
        }

        List<BackendApiClient.ProjectDetails> exactMatches = cachedProjects.stream()
                .filter(project -> exactMatchScore(project, query) == 0)
                .limit(2)
                .toList();
        if (exactMatches.size() == 1) {
            return Optional.of(exactMatches.getFirst());
        }
        return Optional.empty();
    }

    public String formatProjectLabel(BackendApiClient.ProjectDetails project) {
        String primary = firstNonBlank(project.displayName, project.name, project.pathName, "project");
        return project.id + ":" + primary;
    }

    public boolean hasBounds(BackendApiClient.ProjectDetails project) {
        return project != null
                && (project.worldName != null || project.worldMountName != null)
                && project.minX != null && project.minY != null && project.minZ != null
                && project.maxX != null && project.maxY != null && project.maxZ != null;
    }

    public boolean isInsideProject(Location location, BackendApiClient.ProjectDetails project) {
        if (location.getWorld() == null || project == null || !hasBounds(project)) {
            return false;
        }
        if (!matchesWorld(location, project)) {
            return false;
        }
        return location.getX() >= project.minX && location.getX() <= project.maxX + 1
                && location.getY() >= project.minY && location.getY() <= project.maxY + 1
                && location.getZ() >= project.minZ && location.getZ() <= project.maxZ + 1;
    }

    public boolean matchesWorld(Location location, BackendApiClient.ProjectDetails project) {
        if (location == null || location.getWorld() == null || project == null) {
            return false;
        }
        String worldName = location.getWorld().getName();
        return Objects.equals(project.worldName, worldName) || Objects.equals(project.worldMountName, worldName);
    }

    private boolean matches(BackendApiClient.ProjectDetails project, String query) {
        return contains(normalize(project.name), query)
                || contains(normalize(project.displayName), query)
                || contains(normalize(project.pathName), query)
                || contains(normalize(project.kind), query)
                || contains(normalize(project.worldName), query)
                || contains(String.valueOf(project.id), query);
    }

    private int exactMatchScore(BackendApiClient.ProjectDetails project, String query) {
        if (query.equals(normalize(String.valueOf(project.id)))) return 0;
        if (query.equals(normalize(project.displayName))) return 0;
        if (query.equals(normalize(project.name))) return 0;
        if (query.equals(normalize(project.pathName))) return 0;
        return 1;
    }

    private int containsScore(BackendApiClient.ProjectDetails project, String query) {
        if (startsWith(normalize(project.displayName), query)) return 0;
        if (startsWith(normalize(project.name), query)) return 1;
        if (startsWith(normalize(project.pathName), query)) return 2;
        return 3;
    }

    private boolean contains(String target, String query) {
        return !target.isEmpty() && target.contains(query);
    }

    private boolean startsWith(String target, String query) {
        return !target.isEmpty() && target.startsWith(query);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private Long parseProjectId(String rawIdentifier) {
        if (rawIdentifier == null || rawIdentifier.isBlank()) {
            return null;
        }
        String trimmed = rawIdentifier.trim();
        String idPart = trimmed.contains(":") ? trimmed.substring(0, trimmed.indexOf(':')) : trimmed;
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
