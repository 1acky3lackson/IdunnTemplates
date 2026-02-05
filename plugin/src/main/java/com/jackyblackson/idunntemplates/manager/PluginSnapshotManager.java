package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.util.EntityHelper;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PluginSnapshotManager {

    private final IdunnTemplates plugin;
    private final String renderServiceUrl;
    private final String backendDownloadBaseUrl;
    private final boolean on;

    // --- 队列系统 ---
    private final Queue<RenderTask> taskQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_TICKS = 60L; // 失败后等待3秒

    // --- 状态与连接管理 ---
    private volatile HttpURLConnection activeConn = null; // 用于在清理时强行中断当前请求

    public PluginSnapshotManager(IdunnTemplates plugin) {
        this.plugin = plugin;
        this.on = plugin.getConfig().getBoolean("services.thumbnail-generation.available", false);
        if (!this.on) {
            plugin.getLogger().info("Auto thumbnail generation service is off.");
        }
        this.renderServiceUrl = plugin.getConfig().getString("services.thumbnail-generation.renderer-url", "http://localhost:3000");
        this.backendDownloadBaseUrl = plugin.getConfig().getString("services.thumbnail-generation.backend-api-url", "http://localhost:8080");
    }

    /**
     * 清除所有任务并中断当前的渲染连接
     * 建议在插件 reload 或 disable 时调用
     */
    public void clearTasks() {
        int pendingCount = taskQueue.size();
        taskQueue.clear();

        // 重置状态，切断 processNextTask 的递归链
        isProcessing.set(false);

        // 如果当前有正在阻塞的 HTTP 连接，强行断开
        if (activeConn != null) {
            plugin.getLogger().info("Interrupting active render connection...");
            // 在新线程中执行断开，避免阻塞主逻辑
            new Thread(() -> {
                try {
                    if (activeConn != null) {
                        activeConn.disconnect();
                        activeConn = null;
                    }
                } catch (Exception ignored) {}
            }).start();
        }

        plugin.getLogger().info("Cleared " + pendingCount + " pending thumbnail tasks and reset state.");
    }

    /**
     * 检查并为模版的所有角度生成缩略图 (加入队列)
     */
    public void checkAndGenerateThumbnails(Template template, boolean force) {
        if (!this.on || this.backendDownloadBaseUrl == null || this.renderServiceUrl == null) {
            return;
        }

        TemplateVersion latest = template.getLatestVersion();
        if (latest == null) return;

        for (int angle = 0; angle < 4; angle++) {
            RenderTask task = new RenderTask(template, latest.getVersionId(), angle, force);
            taskQueue.offer(task);
        }

        triggerProcessor();
    }

    private void triggerProcessor() {
        if (isProcessing.compareAndSet(false, true)) {
            processNextTask();
        }
    }

    private void processNextTask() {
        // 安全检查：如果队列为空或插件已禁用，停止处理
        if (!plugin.isEnabled() || !this.on) {
            isProcessing.set(false);
            return;
        }

        RenderTask task = taskQueue.poll();

        if (task == null) {
            isProcessing.set(false);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean shouldRetry = false;
            try {
                processSingleTask(task);
            } catch (Exception e) {
                // 如果是主动中断连接导致的异常，不计入错误重试
                if (plugin.isEnabled() && isProcessing.get()) {
                    plugin.getLogger().warning("Error processing thumbnail task for " + task.templateName + ": " + e.getMessage());
                    shouldRetry = true;
                }
            }

            // 结果处理与调度
            if (shouldRetry) {
                if (task.retryCount < MAX_RETRIES) {
                    task.retryCount++;
                    taskQueue.offer(task);
                    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::processNextTask, RETRY_DELAY_TICKS);
                } else {
                    plugin.getLogger().severe("Given up generating thumbnail for " + task.templateName + " after " + MAX_RETRIES + " attempts.");
                    processNextTask();
                }
            } else {
                processNextTask();
            }
        });
    }

    private void processSingleTask(RenderTask task) throws Exception {
        File templateDir = EntityHelper.getDirectory(task.template);
        String versionId = task.versionId;
        int angle = task.angle;

        String targetFilename = "thumbnail_angle" + angle + "_v" + versionId + ".png";
        File targetFile = new File(templateDir, targetFilename);

        cleanStaleThumbnails(templateDir, angle, versionId);

        if (targetFile.exists() && !task.force) {
            return;
        }

        String downloadUrl = String.format("%s/api/v1/templates/%s/download?version=",
                backendDownloadBaseUrl, task.templateId);

        double angleParam = Math.PI * (0.25 + ((double) angle) / 2.0);

        plugin.getLogger().info("Rendering: " + task.templateName + " [Angle " + angle + "] (Queue size: " + taskQueue.size() + ")");

        boolean success = callRenderService(downloadUrl, targetFile, angleParam);

        if (!success) {
            throw new IOException("Render service returned failure.");
        }
    }

    private void cleanStaleThumbnails(File dir, int angle, String currentVersionId) {
        if (!dir.exists()) return;
        File[] stale = dir.listFiles((d, name) ->
                name.startsWith("thumbnail_angle" + angle) &&
                        name.endsWith(".png") &&
                        !name.contains("_v" + currentVersionId + ".png")
        );

        if (stale != null) {
            for (File f : stale) {
                f.delete();
            }
        }
    }

    private boolean callRenderService(String schematicUrl, File outputFile, double angle) {
        HttpURLConnection conn = null;
        try {
            URL url = new URI(renderServiceUrl + "/api/render").toURL();
            conn = (HttpURLConnection) url.openConnection();
            this.activeConn = conn; // 保存当前连接引用以便必要时中断

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(600000); // 10分钟

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

            int code = conn.getResponseCode();
            if (code == 200) {
                try (InputStream is = conn.getInputStream()) {
                    if (!outputFile.getParentFile().exists()) {
                        outputFile.getParentFile().mkdirs();
                    }
                    Files.copy(is, outputFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    return true;
                }
            } else {
                plugin.getLogger().warning("Render service error code: " + code);
                return false;
            }
        } catch (Exception e) {
            // 只有当插件还开启时才记录连接报错
            if (plugin.isEnabled() && isProcessing.get()) {
                plugin.getLogger().severe("Render service connection failed: " + e.getMessage());
            }
            return false;
        } finally {
            if (conn != null) conn.disconnect();
            this.activeConn = null; // 释放引用
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