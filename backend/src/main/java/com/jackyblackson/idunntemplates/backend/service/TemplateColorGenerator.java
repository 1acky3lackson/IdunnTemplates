package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.domain.TemplateColorScheme;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateColorSchemeRepository;
import com.jackyblackson.idunntemplates.backend.util.SimpleColorThief;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TemplateColorGenerator {

    private final TemplateColorSchemeRepository repository;
    private final TemplateVersionService versionService;

    @Value("${app.storage.schematic-root-dir:schematics}")
    private String schematicRootDirPath;

    public TemplateColorGenerator(TemplateColorSchemeRepository repository, TemplateVersionService versionService) {
        this.repository = repository;
        this.versionService = versionService;
    }

    /**
     * 核心逻辑：生成色系并存入数据库
     * 使用 REQUIRES_NEW 确保即使外部事务失败，生成的颜色缓存也能独立提交（或根据业务需求调整）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<String> generateColorScheme(Template template, String versionName) {
        List<File> images = new ArrayList<>();
        // 1. Get images (0-3)
        for (int i = 0; i < 4; i++) {
            File f = getThumbnailFile(template, i);
            if (f != null && f.exists()) {
                images.add(f);
            }
        }

        if (images.isEmpty()) {
            return Collections.nCopies(6, "unknown");
        }

        try {
            // 2. Combine images
            List<BufferedImage> bufferedImages = new ArrayList<>();
            int totalWidth = 0;
            int maxHeight = 0;

            for (File imgFile : images) {
                try {
                    BufferedImage bi = ImageIO.read(imgFile);
                    if (bi != null) {
                        bufferedImages.add(bi);
                        totalWidth += bi.getWidth();
                        maxHeight = Math.max(maxHeight, bi.getHeight());
                    }
                } catch (Exception e) {
                    // Ignore individual image read failures
                }
            }

            if (bufferedImages.isEmpty()) {
                return Collections.nCopies(6, "unknown");
            }

            BufferedImage combined = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = combined.createGraphics();
            int x = 0;
            for (BufferedImage bi : bufferedImages) {
                g.drawImage(bi, x, 0, null);
                x += bi.getWidth();
            }
            g.dispose();

            // 3. Extract colors
            List<String> colors = SimpleColorThief.getPalette(combined, 6);

            while (colors.size() < 6) {
                colors.add("unknown");
            }
            if (colors.size() > 6) {
                colors = colors.subList(0, 6);
            }

            // 4. Save
            TemplateVersion latest = template.getLatestVersion();
            if (latest == null) {
                latest = versionService.getLatestVersion(template.getId()).orElse(null);
            }
            String colorsStr = String.join(",", colors);

            TemplateColorScheme scheme = repository.findByTemplateId(template.getId())
                    .orElse(new TemplateColorScheme(template.getId(), versionName, colorsStr));

            scheme.setVersion(versionName);
            scheme.setColors(colorsStr);
            repository.save(scheme);

            return colors;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.nCopies(6, "unknown");
        }
    }

    public String getColorSchemVersionId(Template template) {
        TemplateVersion latestVersion = template.getLatestVersion();
        if (latestVersion == null) {
            latestVersion = versionService.getLatestVersion(template.getId()).orElse(null);
        }
        if (latestVersion == null) {
            return "unknown";
        }
        StringBuilder versionIdSb = new StringBuilder(latestVersion.getVersionId()).append("-");
        for (int i = 0; i < 4; i++) {
            File f = getThumbnailFile(template, i);
            if (f != null && f.exists()) {
                versionIdSb.append(i);
            }
        }
        return versionIdSb.toString();
    }

    private File getThumbnailFile(Template template, int angle) {
        try {
            TemplateVersion latestVersion = template.getLatestVersion();
            if (latestVersion == null) {
                latestVersion = versionService.getLatestVersion(template.getId()).orElse(null);
            }

            if (latestVersion == null) return null;

            File rootDir = new File(schematicRootDirPath);
            File templateDir = new File(rootDir, template.getPath());
            String targetFilename = "thumbnail_angle" + (angle % 4) + "_v" + latestVersion.getVersionId() + ".webp";
            return new File(templateDir, targetFilename);
        } catch (Exception e) {
            return null;
        }
    }
}