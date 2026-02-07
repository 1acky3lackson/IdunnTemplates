package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.backend.dto.TemplateThumbnailInfo;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.backend.store.spec.TemplateSpecifications;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionService templateVersionService;
    private final SnapshotService snapshotService;
    private final TemplateColorService templateColorService;

    private final LuckyPermAuthService luckyPermAuthService;

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
                           SnapshotService snapshotService,
                           LuckyPermAuthService luckyPermAuthService,
                           TemplateColorService templateColorService
    ) {
        this.templateRepository = templateRepository;
        this.templateVersionService = templateVersionService;
        this.snapshotService = snapshotService;
        this.luckyPermAuthService = luckyPermAuthService;
        this.templateColorService = templateColorService;
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
    public Page<Template> searchTemplates(TemplateSearchCriteria criteria, Pageable pageable, UserContext userContext) {
        // 1. 数据库查询 (获取原始分页结果)
        // 这一步很快，且利用了数据库索引
        // 如果 criteria 要求按最新版本排序
        if (Boolean.TRUE.equals(criteria.getSortByLatestVersionTime())) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "lastVersionAt").and(Sort.by(Sort.Direction.DESC, "id"))
            );
        }

        Page<Template> dbResult = templateRepository.findAll(
                TemplateSpecifications.withCriteria(criteria),
                pageable
        );

        // 如果数据库里都没查到，直接返回空 Page，省去鉴权开销
        if (dbResult.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 内存鉴权过滤 (Post-Filtering)
        // 使用刚才写的 filterList 泛型方法
        List<Template> filteredContent = luckyPermAuthService.filterList(
                userContext.getUuid(),
                userContext.getUsername(),
                dbResult.getContent(),      // 原始列表
                Template::getUsePermissionNode, // 提取权限节点的 Mapper
                true
        );


        // 3. 重新封装成 Page 对象
        // 注意：
        // - 第一个参数是过滤后的内容 (可能比 pageSize 小，甚至为空)
        // - 第二个参数是原本的分页请求信息
        // - 第三个参数是【数据库里的总条数】(User sees this "fake" total)
        //
        // 为什么用 dbResult.getTotalElements()？
        // 因为我们不知道过滤后到底剩多少条，除非把全库查出来跑一遍鉴权（性能自杀）。
        // 所以我们保留数据库的总数，告诉前端“大概还有这么多，但不保证都能看”。
        return new PageImpl<>(
                filteredContent,
                pageable,
                dbResult.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public Optional<Template> getTemplateById(UUID id, UserContext user) {
        Template template = templateRepository.findById(id).orElseThrow();
        boolean hasPerm = luckyPermAuthService.checkPermission(user.getUuid(), user.getUsername(), template.getUsePermissionNode());
        if (hasPerm) {
            return Optional.of(template);
        } else {
            return Optional.empty();
        }
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
     * [修改后] 获取 WebP 格式的缩略图文件
     */
    public File getThumbnailFile(UUID templateId, int angle) throws FileNotFoundException {
        int filteredAngle = angle % 4;

        // 1. 获取基础信息
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new FileNotFoundException("Template not found: " + templateId));

        TemplateVersion latestVersion = templateVersionService.getLatestVersion(templateId)
                .orElseThrow(() -> new FileNotFoundException("No versions found for template: " + templateId));

        // 2. 确定目标文件路径
        File rootDir = new File(schematicRootDirPath);
        File templateDir = new File(rootDir, template.getPath());

        // [变更点] 后缀名改为 .webp
        String targetFilename = "thumbnail_angle" + filteredAngle + "_v" + latestVersion.getVersionId() + ".webp";
        File targetFile = new File(templateDir, targetFilename);

        // 3. 仅检查是否存在
        if (targetFile.exists() && targetFile.isFile()) {
            return targetFile;
        }

        // 4. 异常处理
        throw new FileNotFoundException("Thumbnail image not found on disk: " + targetFilename);
    }

    public TemplateThumbnailInfo getThumbnailInfo(UUID templateId, UserContext user) throws FileNotFoundException {
        // ... 原有权限检查逻辑保持不变 ...
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new FileNotFoundException("Template not found: " + templateId));
        if (!luckyPermAuthService.checkPermission(user.getUuid(), user.getUsername(), template.getUsePermissionNode())) {
            throw new RuntimeException("You have no permission to access the thumbnail");
        }
        TemplateVersion latest = templateVersionService.getLatestVersion(templateId)
                .orElseThrow(() -> new FileNotFoundException("No versions found for template: " + templateId));

        // 注意：如果 TemplateThumbnailInfo 包含文件扩展名或完整 URL，
        // 你可能需要更新这个类的构造函数或者让前端知道现在默认是 webp
        return new TemplateThumbnailInfo(template.getPath(), latest.getVersionId());
    }

    /**
     * [修改后] 保存时将 PNG 字节流转换为 WebP 文件
     */
    public void saveThumbnail(String templatePath, String version, int angle, byte[] imageBytes) throws IOException {
        int filteredAngle = angle % 4;
        File rootDir = new File(schematicRootDirPath);
        File templateDir = new File(rootDir, templatePath);

        if (!templateDir.exists()) {
            boolean mkdirsSuccess = templateDir.mkdirs();
            // 建议加上简单的检查
            if (!mkdirsSuccess && !templateDir.exists()) {
                throw new IOException("Failed to create directory: " + templateDir.getAbsolutePath());
            }
        }

        // 1. 读取原始图片 (假设是 PNG 或 JPEG 等常见格式，ImageIO 会自动识别)
        BufferedImage image;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            image = ImageIO.read(bis);
        }

        if (image == null) {
            throw new IOException("Failed to decode uploaded image bytes. Is the format supported?");
        }

        // [变更点] 后缀名改为 .webp
        String targetFilename = "thumbnail_angle" + filteredAngle + "_v" + version + ".webp";
        File targetFile = new File(templateDir, targetFilename);

        // 2. 写入 WebP 格式
        // 只要引入了 TwelveMonkeys 依赖，这里就可以直接写 "webp"
        boolean success = ImageIO.write(image, "webp", targetFile);

        if (!success) {
            throw new IOException("No WebP writer found. Please ensure 'com.twelvemonkeys.imageio:imageio-webp' dependency is added.");
        }
    }

}
