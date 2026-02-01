package com.jackyblackson.idunntemplates.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
public class SnapshotService {

    // Node 服务的地址
    @Value("${app.service.schematic-renderer-base-url}")
    private String RENDER_SERVICE_URL = "http://localhost:3000";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用外部渲染服务生成缩略图
     * @param schematicDownloadUrl 此 Schematic 的可公开访问下载链接 (或局域网互通链接)
     * @param outputFile 结果保存路径
     */
    public void generateSnapshot(String schematicDownloadUrl, File outputFile, double angle) {
        try {
            // 1. 准备请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("schematicUrl", schematicDownloadUrl);
            requestBody.put("width", 800);
            requestBody.put("height", 600);
            requestBody.put("alpha", angle);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 2. 发送请求
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    RENDER_SERVICE_URL + "/api/render",
                    entity,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 3. 保存图片
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                Files.write(outputFile.toPath(), response.getBody());
                System.out.println("Snapshot generated successfully: " + outputFile.getName());
            } else {
                System.err.println("Snapshot generation failed: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error calling render service: " + e.getMessage());
        }
    }
}