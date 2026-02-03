package com.jackyblackson.idunntemplates.manager;

import com.google.gson.Gson;
import com.jackyblackson.idunntemplates.IdunnTemplates;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.util.EntityHelper;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
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
    private static final long RETRY_DELAY_TICKS = 60L; // 失败后等待3秒再处理下一个任务（防止死循环刷屏）
    private final String backendAuthUrl;
    private final String authUsername;
    private final String authPassword;
    // --- 状态管理 ---
    private String authToken = null; // 缓存的 Cookie 值 (auth_token=xxx)

    public PluginSnapshotManager(IdunnTemplates plugin) {
        this.plugin = plugin;
        this.on = plugin.getConfig().getBoolean("services.thumbnail-generation.available", false);
        if (!this.on) {
            plugin.getLogger().info("Auto thumbnail generation service is off. Templates will not have their thumbnail images generated.");
        }
        this.renderServiceUrl = plugin.getConfig().getString("services.thumbnail-generation.renderer-url", "http://localhost:3000");
        this.backendDownloadBaseUrl = plugin.getConfig().getString("services.thumbnail-generation.backend-api-url", "http://localhost:8080");
        this.backendAuthUrl = backendDownloadBaseUrl + "/auth/login";

        this.authUsername = plugin.getConfig().getString("services.thumbnail-generation.yggdrasil-username", "Idunn_Admin");
        this.authPassword = plugin.getConfig().getString("services.thumbnail-generation.yggdrasil-password", "");
    }

    /**
     * 检查并为模版的所有角度生成缩略图 (加入队列)
     * @param template 模版对象
     * @param force 是否强制重新生成
     */
    public void checkAndGenerateThumbnails(Template template, boolean force) {
        if (!this.on || this.backendDownloadBaseUrl == null || this.renderServiceUrl == null) {
            return;
        }

        TemplateVersion latest = template.getLatestVersion();
        if (latest == null) return;

        // 将4个角度拆分为4个独立的任务加入队列
        for (int angle = 0; angle < 4; angle++) {
            RenderTask task = new RenderTask(template, latest.getVersionId(), angle, force);
            taskQueue.offer(task);
        }

        // 尝试启动消费者
        triggerProcessor();
    }

    /**
     * 触发队列处理。如果已经在处理中，则忽略。
     */
    private void triggerProcessor() {
        // CAS 操作确保只有一个线程在跑循环
        if (isProcessing.compareAndSet(false, true)) {
            processNextTask();
        }
    }

    /**
     * 处理队列中的下一个任务 (递归调度)
     */
    private void processNextTask() {
        RenderTask task = taskQueue.poll();

        // 1. 队列为空，停止处理
        if (task == null) {
            isProcessing.set(false);
            return;
        }

        // 2. 异步执行当前任务
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean shouldRetry = false;
            try {
                processSingleTask(task);
            } catch (Exception e) {
                plugin.getLogger().warning("Error processing thumbnail task for " + task.templateName + ": " + e.getMessage());
                shouldRetry = true;
            }

            // 3. 结果处理与调度
            if (shouldRetry) {
                if (task.retryCount < MAX_RETRIES) {
                    task.retryCount++;
                    plugin.getLogger().info("Retrying thumbnail for " + task.templateName + " (Angle " + task.angle + ") later... [Attempt " + task.retryCount + "]");
                    taskQueue.offer(task); // 放回队尾

                    // 失败延迟调度：稍微休息一下再处理下一个，避免网络故障时疯狂刷报错
                    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::processNextTask, RETRY_DELAY_TICKS);
                } else {
                    plugin.getLogger().severe("Given up generating thumbnail for " + task.templateName + " after " + MAX_RETRIES + " attempts.");
                    // 放弃该任务，立即处理下一个
                    processNextTask();
                }
            } else {
                // 成功，立即处理下一个
                processNextTask();
            }
        });
    }

    /**
     * 执行单个具体的渲染逻辑
     * @return true if success or skipped, false if failed and needs retry
     */
    private void processSingleTask(RenderTask task) throws Exception {
        File templateDir = EntityHelper.getDirectory(task.template);
        String versionId = task.versionId;
        int angle = task.angle;

        // 构造文件名
        String targetFilename = "thumbnail_angle" + angle + "_v" + versionId + ".png";
        File targetFile = new File(templateDir, targetFilename);

        // 1. 清理旧版本
        cleanStaleThumbnails(templateDir, angle, versionId);

        // 2. 检查是否需要生成
        if (targetFile.exists() && !task.force) {
            return; // 视为成功跳过
        }

        // 3. 构造 Node 服务参数
        String downloadUrl = String.format("%s/api/v1/templates/%s/download?version=",
                backendDownloadBaseUrl, task.templateId);
        plugin.getLogger().info("SchemSourceUrl: " + downloadUrl);

        double angleParam = Math.PI * (0.25 + ((double) angle) / 2.0);

        plugin.getLogger().info("Rendering: " + task.templateName + " [Angle " + angle + "] (Queue size: " + taskQueue.size() + ")");

        // 4. 调用渲染服务 (阻塞调用)
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
            URL url = new URL(renderServiceUrl + "/api/render");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "image/png");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(6000000); // 增加超时时间，渲染可能很慢

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
            plugin.getLogger().severe("Render service connection failed: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * 内部任务对象
     */
    private static class RenderTask {
        // 不直接持有 Template 对象防止内存泄漏或异步访问问题，
        // 但这里为了方便获取路径，持有 Template 是可以接受的，只要 Template 对象本身是常驻内存的
        final Template template;
        final String templateId; // 冗余存ID方便日志
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