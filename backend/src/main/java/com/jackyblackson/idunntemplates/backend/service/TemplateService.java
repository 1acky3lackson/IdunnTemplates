package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.backend.store.spec.TemplateSpecifications;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionService templateVersionService;
    private final SnapshotService snapshotService;

    // 从 application.properties 读取 schematic 存储根目录
    // 对应 app.datasource.sqlite.file-path 所在的父级目录或者专门配置的目录
    @Value("${app.storage.schematic-root-dir:schematics}")
    private String schematicRootDirPath;

    // 获取当前服务端口，用于构造回调 URL
    @Value("${server.port:8080}")
    private String serverPort;

    // 获取当前服务内部 IP/Host，Docker 环境通常是服务名，本地是 localhost
    @Value("${app.server.internal-host:localhost}")
    private String serverHost;

    @Autowired
    public TemplateService(TemplateRepository templateRepository,
                           TemplateVersionService templateVersionService,
                           SnapshotService snapshotService) {
        this.templateRepository = templateRepository;
        this.templateVersionService = templateVersionService;
        this.snapshotService = snapshotService;
    }

    @Deprecated
    @Transactional(readOnly = true)
    public List<Template> getTemplatesByPath(String path) {
        return templateRepository.findByPathStartingWith(path);
    }

    /**
     * 复合条件搜索 + 分页 + 排序
     */
    @Transactional(readOnly = true)
    public Page<Template> searchTemplates(TemplateSearchCriteria criteria, Pageable pageable) {
        // 将 DTO 转换为 Specification
        var spec = TemplateSpecifications.withCriteria(criteria);
        // 执行查询
        return templateRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Template> getTemplateById(UUID id) {
        return templateRepository.findById(id);
    }

    /**
     * 获取指定版本的 Schematic 文件对象
     */
    public File getSchematicFile(UUID templateId, String versionId) throws FileNotFoundException {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new FileNotFoundException("Template not found: " + templateId));

        // 如果 versionId 为 null，尝试获取最新版本
        String targetVersionId = versionId;
        if (targetVersionId == null || targetVersionId.isEmpty() || "latest".equalsIgnoreCase(targetVersionId)) {
            TemplateVersion latest = templateVersionService.getLatestVersion(templateId)
                    .orElseThrow(() -> new FileNotFoundException("No versions found for template: " + templateId));
            targetVersionId = latest.getVersionId();
        }

        // 路径逻辑复刻：Root / TemplatePath / VersionId.schem
        // Template.path 在数据库中存储的是完整相对路径 (e.g., "users/Jacky/castle")
        File rootDir = new File(schematicRootDirPath);
        File templateDir = new File(rootDir, template.getPath());
        File schematicFile = new File(templateDir, targetVersionId + ".schem");

        if (!schematicFile.exists()) {
            throw new FileNotFoundException("Schematic file not found on disk: " + schematicFile.getAbsolutePath());
        }

        return schematicFile;
    }

    /**
     * [核心逻辑] 获取缩略图，如果不存在或版本过期则自动生成
     */
    public File getOrGenerateThumbnail(UUID templateId, boolean forced, int angle) throws FileNotFoundException {
        int filteredAngle = angle % 4;
        // 1. 获取基础信息
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new FileNotFoundException("Template not found: " + templateId));

        TemplateVersion latestVersion = templateVersionService.getLatestVersion(templateId)
                .orElseThrow(() -> new FileNotFoundException("No versions found for template: " + templateId));

        // 2. 确定文件路径
        File rootDir = new File(schematicRootDirPath);
        File templateDir = new File(rootDir, template.getPath());

        // 目标文件名格式：thumbnail_{versionId}.png
        // 这样做的好处是浏览器缓存永远不会错，因为版本更新文件名就变了
        String targetFilename = "thumbnail_angle" + String.valueOf(filteredAngle) + "_v" + latestVersion.getVersionId() + ".png";
        File targetFile = new File(templateDir, targetFilename);

        // 3. 清理旧版本缩略图 (Clean up stale thumbnails)
        if (templateDir.exists() && templateDir.isDirectory()) {
            File[] staleThumbnails = templateDir.listFiles((dir, name) ->
                    name.startsWith("thumbnail_angle" + String.valueOf(filteredAngle)) && name.endsWith(".png") && !name.equals(targetFilename)
            );

            if (staleThumbnails != null) {
                for (File f : staleThumbnails) {
                    if (f.delete()) {
                        System.out.println("Deleted stale thumbnail: " + f.getName());
                    }
                }
            }
        }

        // 4. 检查目标文件是否存在
        if (targetFile.exists() && !forced) {
            return targetFile; // 命中缓存，直接返回
        }

        // 5. 不存在，调用 SnapshotService 生成
        // 构造一个 Node 服务可以访问回来的下载链接
        // 格式: http://{host}:{port}/api/v1/templates/{id}/download?version={ver}
        String callbackUrl = String.format("http://%s:%s/api/v1/templates/%s/download?version=%s",
                serverHost, serverPort, templateId, latestVersion.getVersionId());

        System.out.println("Generating thumbnail for " + template.getName() + " via: " + callbackUrl);

        double angleParam = Math.PI * ( 0.25 + ((float) filteredAngle) / 2 );

        // 这一步是同步阻塞的，直到图片生成完毕
        snapshotService.generateSnapshot(callbackUrl, targetFile, angleParam);

        // 6. 再次检查是否生成成功
        if (!targetFile.exists()) {
            throw new FileNotFoundException("Thumbnail generation failed, file not created.");
        }

        return targetFile;
    }
}
