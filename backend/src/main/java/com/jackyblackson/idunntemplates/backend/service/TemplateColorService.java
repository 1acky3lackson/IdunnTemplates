package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.domain.TemplateColorScheme;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateColorSchemeRepository;
import com.jackyblackson.idunntemplates.backend.util.SimpleColorThief;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TemplateColorService {

    private final TemplateColorSchemeRepository repository;
    private final TemplateVersionService versionService;

    @Value("${app.storage.schematic-root-dir:schematics}")
    private String schematicRootDirPath;

    @Autowired
    private TemplateColorService self;

    @Autowired
    public TemplateColorService(TemplateColorSchemeRepository repository, TemplateVersionService versionService) {
        this.repository = repository;
        this.versionService = versionService;
    }

    /**
     * Batch resolve colors.
     */
    public Map<UUID, List<String>> resolveColorsForTemplates(List<Template> templates) {
        if (templates == null || templates.isEmpty()) return Collections.emptyMap();

        List<UUID> ids = templates.stream().map(Template::getId).collect(Collectors.toList());
        List<TemplateColorScheme> stored = repository.findByTemplateIdIn(ids);
        Map<UUID, TemplateColorScheme> schemeMap = stored.stream()
                .collect(Collectors.toMap(TemplateColorScheme::getTemplateId, s -> s));

        Map<UUID, List<String>> result = new HashMap<>();

        for (Template t : templates) {
            TemplateColorScheme scheme = schemeMap.get(t.getId());
            TemplateVersion latest = t.getLatestVersion();
            String version = latest != null ? latest.getVersionId() : "unknown";

            List<String> colors;
            if (scheme != null && version.equals(scheme.getVersion())) {
                colors = Arrays.asList(scheme.getColors().split(","));
            } else {
                // Call through proxy to ensure transaction
                colors = self.generateColorScheme(t);
            }
            result.put(t.getId(), colors);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<TemplateColorScheme> getStoredSchemes(List<UUID> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) return Collections.emptyList();
        return repository.findByTemplateIdIn(templateIds);
    }

    @Transactional(readOnly = true)
    public Optional<TemplateColorScheme> getStoredScheme(UUID templateId) {
        return repository.findByTemplateId(templateId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<String> generateColorScheme(Template template) {
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
                    // Ignore
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
            String versionId = latest != null ? latest.getVersionId() : "unknown";
            String colorsStr = String.join(",", colors);

            TemplateColorScheme scheme = repository.findByTemplateId(template.getId())
                    .orElse(new TemplateColorScheme(template.getId(), versionId, colorsStr));

            scheme.setVersion(versionId);
            scheme.setColors(colorsStr);
            repository.save(scheme);

            return colors;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.nCopies(6, "unknown");
        }
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
            String targetFilename = "thumbnail_angle" + (angle % 4) + "_v" + latestVersion.getVersionId() + ".png";
            return new File(templateDir, targetFilename);
        } catch (Exception e) {
            return null;
        }
    }
}
