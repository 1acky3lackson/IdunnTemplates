package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.util.EntityHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class PluginSnapshotManager {

    private final IdunnTemplates plugin;
    private final String renderServiceUrl;
    private final String backendDownloadBaseUrl;
    private final boolean on;

    private ExecutorService renderExecutor;
    private ScheduledExecutorService retryScheduler;

    private static final int CONCURRENT_THREADS = 1;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_SECONDS = 3;

    private final Set<HttpURLConnection> activeConnections = ConcurrentHashMap.newKeySet();
    private final AtomicInteger pendingTasksCount = new AtomicInteger(0);

    public PluginSnapshotManager(IdunnTemplates plugin) {
        this.plugin = plugin;
        this.on = plugin.getConfig().getBoolean("services.thumbnail-generation.available", false);
        this.renderServiceUrl = plugin.getConfig().getString("services.thumbnail-generation.renderer-url", "http://localhost:3000");
        this.backendDownloadBaseUrl = plugin.getConfig().getString("services.thumbnail-generation.backend-api-url", "http://localhost:8080");

        if (!this.on) {
            log().info("[Snapshot] Auto thumbnail generation service is disabled in config.");
        } else {
            initExecutors();
            log().info("[Snapshot] Service initialized. Worker threads: " + CONCURRENT_THREADS);
        }
    }

    private Logger log() {
        return IdunnTemplates.getInstance().getLogger();
    }

    private void initExecutors() {
        this.renderExecutor = Executors.newFixedThreadPool(CONCURRENT_THREADS, r -> {
            Thread t = new Thread(r, "Idunn-Render-Worker");
            t.setDaemon(true);
            return t;
        });

        this.retryScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Idunn-Render-Retry-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public synchronized void clearTasks() {
        if (!this.on) return;

        log().info("[Snapshot] Shutting down thumbnail render services...");

        if (renderExecutor != null && !renderExecutor.isShutdown()) {
            renderExecutor.shutdownNow();
        }
        if (retryScheduler != null && !retryScheduler.isShutdown()) {
            retryScheduler.shutdownNow();
        }

        int connCount = activeConnections.size();
        if (connCount > 0) {
            log().info("[Snapshot] Aborting " + connCount + " active render connections.");
            for (HttpURLConnection conn : activeConnections) {
                try { conn.disconnect(); } catch (Exception ignored) {}
            }
            activeConnections.clear();
        }

        pendingTasksCount.set(0);
        this.renderExecutor = null;
        this.retryScheduler = null;
    }

    public void checkAndGenerateThumbnails(Template template, boolean force) {
        if (!this.on || this.backendDownloadBaseUrl == null || this.renderServiceUrl == null) return;

        if (renderExecutor == null || renderExecutor.isShutdown()) {
            initExecutors();
        }

        TemplateVersion latest = template.getLatestVersion();
        if (latest == null) return;

        log().info(String.format("[Snapshot] Scheduling render for template: %s (Version: %s)", template.getName(), latest.getVersionId()));

        for (int angle = 0; angle < 4; angle++) {
            RenderTask task = new RenderTask(template, latest.getVersionId(), angle, force);
            submitTask(task);
        }
    }

    private void submitTask(RenderTask task) {
        if (renderExecutor == null || renderExecutor.isShutdown()) return;

        int queueSize = pendingTasksCount.incrementAndGet();
        try {
            renderExecutor.submit(() -> runTaskLogic(task));
            // 如果堆积过多，输出一条警告
            if (queueSize > 20) {
                log().warning("[Snapshot] Large render queue detected: " + queueSize + " tasks pending.");
            }
        } catch (RejectedExecutionException e) {
            pendingTasksCount.decrementAndGet();
        }
    }

    private void runTaskLogic(RenderTask task) {
        long startTime = System.currentTimeMillis();
        try {
            if (Thread.currentThread().isInterrupted()) return;

            log().info(String.format("[Snapshot] [%s] Starting Angle %d (Attempt %d/%d)",
                    task.templateName, task.angle, task.retryCount + 1, MAX_RETRIES + 1));

            processSingleTask(task);

            long duration = System.currentTimeMillis() - startTime;
            log().info(String.format("[Snapshot] [%s] Completed Angle %d in %dms. Remaining tasks: %d",
                    task.templateName, task.angle, duration, pendingTasksCount.get() - 1));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            if (renderExecutor != null && !renderExecutor.isShutdown()) {
                handleTaskFailure(task, e);
            }
        } finally {
            pendingTasksCount.decrementAndGet();
        }
    }

    private void handleTaskFailure(RenderTask task, Exception e) {
        log().warning(String.format("[Snapshot] [%s] Failed Angle %d: %s", task.templateName, task.angle, e.getMessage()));

        if (task.retryCount < MAX_RETRIES) {
            task.retryCount++;
            if (retryScheduler != null && !retryScheduler.isShutdown()) {
                log().info(String.format("[Snapshot] [%s] Retrying Angle %d in %ds...", task.templateName, task.angle, RETRY_DELAY_SECONDS));
                retryScheduler.schedule(() -> submitTask(task), RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
            }
        } else {
            log().severe(String.format("[Snapshot] [%s] Angle %d failed after %d attempts. Giving up.", task.templateName, task.angle, MAX_RETRIES + 1));
        }
    }

    private void processSingleTask(RenderTask task) throws Exception {
        File templateDir = EntityHelper.getDirectory(task.template);
        String versionId = task.versionId;
        int angle = task.angle;

        String targetFilename = "thumbnail_angle" + angle + "_v" + versionId + ".png";
        File targetFile = new File(templateDir, targetFilename);

        cleanStaleThumbnails(templateDir, angle, versionId);

        if (targetFile.exists() && !task.force) {
            // 这里不抛出异常，只是跳过逻辑，会在 runTaskLogic 中记录完成
            return;
        }

        String downloadUrl = String.format("%s/api/v1/templates/%s/download?version=",
                backendDownloadBaseUrl, task.templateId);

        double angleParam = Math.PI * (0.25 + ((double) angle) / 2.0);

        boolean success = callRenderService(downloadUrl, targetFile, angleParam, task.templateName);

        if (!success) {
            throw new IOException("Render service returned failure or empty response.");
        }
    }

    private void cleanStaleThumbnails(File dir, int angle, String currentVersionId) {
        if (!dir.exists()) return;
        File[] stale = dir.listFiles((d, name) ->
                name.startsWith("thumbnail_angle" + angle) &&
                        name.endsWith(".png") &&
                        !name.contains("_v" + currentVersionId + ".png")
        );

        if (stale != null && stale.length > 0) {
            for (File f : stale) {
                if (f.delete()) {
                    log().fine("[Snapshot] Deleted stale thumbnail: " + f.getName());
                }
            }
        }
    }

    private boolean callRenderService(String schematicUrl, File outputFile, double angle, String debugName) throws InterruptedException {
        HttpURLConnection conn = null;
        try {
            URL url = new URI(renderServiceUrl + "/api/render").toURL();
            log().info("[Snapshot] Connect to render service at: " + url);
            log().info("           with schem download url: " + schematicUrl);
            conn = (HttpURLConnection) url.openConnection();
            activeConnections.add(conn);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000); // 缩短为2分钟，10分钟太夸张了

            Map<String, Object> data = new HashMap<>();
            data.put("schematicUrl", schematicUrl);
            data.put("width", 800);
            data.put("height", 600);
            data.put("alpha", angle);

            String jsonInput = new Gson().toJson(data);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

            int code = conn.getResponseCode();
            if (code == 200) {
                File tempFile = new File(outputFile.getParentFile(), outputFile.getName() + ".tmp");
                try (InputStream is = conn.getInputStream()) {
                    if (!tempFile.getParentFile().exists()) tempFile.getParentFile().mkdirs();
                    Files.copy(is, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                Files.move(tempFile.toPath(), outputFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return true;
            } else {
                log().warning("[Snapshot] Render API error (" + code + ") for " + debugName);
                return false;
            }
        } catch (InterruptedException ie) {
            throw ie;
        } catch (Exception e) {
            if (renderExecutor != null && !renderExecutor.isShutdown()) {
                log().severe("[Snapshot] Connection failed for " + debugName + ": " + e.getMessage());
            }
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
                activeConnections.remove(conn);
            }
        }
    }

    private static class RenderTask {
        final Template template;
        final String templateId;
        final String templateName;
        final String versionId;
        final int angle;
        final boolean force;
        int retryCount = 0;

        public RenderTask(Template template, String versionId, int angle, boolean force) {
            this.template = template;
            this.templateId = template.getId().toString();
            this.templateName = template.getName();
            this.versionId = versionId;
            this.angle = angle;
            this.force = force;
        }
    }
}