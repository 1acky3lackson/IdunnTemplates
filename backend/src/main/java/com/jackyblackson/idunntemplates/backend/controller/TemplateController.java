package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.backend.dto.TemplateInstanceDto;
import com.jackyblackson.idunntemplates.backend.dto.TemplateWithColorsDto;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.backend.service.SchematicFormatService;
import com.jackyblackson.idunntemplates.backend.service.TemplateColorService;
import com.jackyblackson.idunntemplates.backend.service.TemplateService;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateVersionRepository;
import com.jackyblackson.idunntemplates.backend.util.CollectionUtils;
import com.jackyblackson.idunntemplates.backend.util.JwtUtil;
import com.jackyblackson.idunntemplates.backend.dto.TemplateThumbnailInfo;
import com.jackyblackson.idunntemplates.backend.dto.ThumbnailUploadRequestDto;
import com.jackyblackson.idunntemplates.backend.dto.ThumbnailUploadTokenDto;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.Base64;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.jpa.domain.Specification;
import pitheguy.schemconvert.converter.ConversionException;
import pitheguy.schemconvert.converter.formats.SchemSchematicFormat;
import pitheguy.schemconvert.converter.formats.SchematicFormat;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateColorService templateColorService;

    private final SchematicFormatService schematicFormatService;
    private final JwtUtil jwtUtil;
    private final LuckyPermAuthService luckyPermAuthService;
    private final TemplateVersionRepository templateVersionRepository;
    private final com.jackyblackson.idunntemplates.backend.store.repository.InstanceRepository instanceRepository;

    @Autowired
    public TemplateController(TemplateService templateService,
                              TemplateColorService templateColorService,
                              SchematicFormatService schematicFormatService,
                              JwtUtil jwtUtil,
                              LuckyPermAuthService luckyPermAuthService,
                              TemplateVersionRepository templateVersionRepository,
                              com.jackyblackson.idunntemplates.backend.store.repository.InstanceRepository instanceRepository) {
        this.templateService = templateService;
        this.templateColorService = templateColorService;
        this.schematicFormatService = schematicFormatService;
        this.jwtUtil = jwtUtil;
        this.luckyPermAuthService = luckyPermAuthService;
        this.templateVersionRepository = templateVersionRepository;
        this.instanceRepository = instanceRepository;
    }

    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<TemplateWithColorsDto>> searchTemplates(
            @ModelAttribute TemplateSearchCriteria criteria,
            UserContext userContext,
            @PageableDefault(size = 20, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Template> page = templateService.searchTemplates(criteria, pageable, userContext);
        List<Template> originalContent = page.getContent();

        // 1. 提取非空的 ID 和 Path 用于批量查询，避免对 null 对象调用方法
        List<Template> nonNullTemplates = originalContent.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2. 批量解析颜色 (Map 的 Key 是 UUID)
        Map<UUID, List<String>> colors = nonNullTemplates.isEmpty() ? Collections.emptyMap() :
                templateColorService.resolveColorsForTemplates(nonNullTemplates);

        // 3. 批量检查权限 (需处理 path 为 null 的情况)
        List<String> distinctPaths = nonNullTemplates.stream()
                .map(Template::getPath)
                .filter(Objects::nonNull)
                .map(path -> PermissionNames.Templates.commitToPath$R + "." + path.replace("/", "."))
                .distinct()
                .collect(Collectors.toList());

        Map<String, Boolean> commitPermResults = (userContext != null && !distinctPaths.isEmpty()) ?
                luckyPermAuthService.batchCheckPermissions(userContext.getUuid(), userContext.getUsername(), distinctPaths) :
                Collections.emptyMap();

        // 4. 批量获取版本 (关联查询)
        Map<UUID, List<TemplateVersion>> versionsMap = nonNullTemplates.isEmpty() ? Collections.emptyMap() :
                templateVersionRepository.findByTemplateIn(nonNullTemplates).stream()
                        .filter(v -> v != null && v.getTemplate() != null)
                        .collect(Collectors.groupingBy(v -> v.getTemplate().getId()));

        // 5. 映射 DTO，严格保持 originalContent 的顺序和长度
        List<TemplateWithColorsDto> dtos = originalContent.stream().map(t -> {
            // 如果元素为 null，直接返回 null 保持占位
            if (t == null) {
                return null;
            }

            // 此时 t 保证非空
            List<String> colorList = colors.getOrDefault(t.getId(), Collections.emptyList());
            TemplateWithColorsDto dto = new TemplateWithColorsDto(t, colorList);

            // 处理版本信息
            List<TemplateVersion> allVersions = versionsMap.getOrDefault(t.getId(), Collections.emptyList());
            List<TemplateVersion> sortedVersions = allVersions.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingLong((TemplateVersion v) ->
                            Optional.of(v.getCreatedAt()).orElse(0L)).reversed())
                    .toList();

            dto.setVersionCount(sortedVersions.size());
            if (!sortedVersions.isEmpty()) {
                dto.setLatestVersionName(sortedVersions.get(0).getVersionId());
                dto.setLatestVersions(sortedVersions.stream().limit(10).collect(Collectors.toList()));
            } else {
                dto.setLatestVersions(Collections.emptyList());
            }

            dto.setCanUse(true);

            // 处理权限
            String path = t.getPath();
            if (path != null) {
                String commitPerm = PermissionNames.Templates.commitToPath$R + "." + path.replace("/", ".");
                dto.setCanCommit(commitPermResults.getOrDefault(commitPerm, false));
            } else {
                dto.setCanCommit(false);
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(dtos, page.getPageable(), page.getTotalElements()));
    }

    @GetMapping("/{id}")
    @AuthRequired
    public ResponseEntity<TemplateWithColorsDto> getTemplate(@PathVariable UUID id, UserContext user) {
        return templateService.getTemplateById(id, user)
                .map(t -> {
                    Map<UUID, List<String>> colors = templateColorService.resolveColorsForTemplates(Collections.singletonList(t));
                    return new TemplateWithColorsDto(t, colors.getOrDefault(t.getId(), Collections.emptyList()));
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/instances")
    @AuthRequired
    public ResponseEntity<Page<TemplateInstanceDto>> listTemplateInstances(
            @PathVariable UUID id,
            @RequestParam(required = false) String search,
            UserContext user,
            @PageableDefault(size = 20, sort = "placedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Optional<Template> templateOptional = templateService.getTemplateById(id, user);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Specification<Instance> specification = buildTemplateInstanceSpecification(id, search);
        Page<Instance> page = instanceRepository.findAll(specification, pageable);
        Page<TemplateInstanceDto> dtoPage = page.map(this::toTemplateInstanceDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}/download")
    @AuthRequired
    public ResponseEntity<Resource> downloadSchematic(
            UserContext user,
            @PathVariable UUID id,
            @RequestParam(required = false) String version,
            @RequestParam(required = false, defaultValue = "schem") String format // 1. 添加参数
    ) {
        File fileToDownload = null;
        boolean isTempFile = false; // 标记是否为临时文件

        SchematicFormat requestedFormat = schematicFormatService.resolveFormat(format);

        Template targetTemplate = templateService.getTemplateById(id, user).orElseThrow();

        try {
            // 获取原始文件 (通常是 .schem)
            File originalFile = templateService.getSchematicFile(id, version);

            // 2. 判断逻辑
            if (requestedFormat instanceof SchemSchematicFormat) {
                // 逻辑 A: 直接下载原文件
                fileToDownload = originalFile;
            } else {
                // 逻辑 B: 格式转换
                try {
                    fileToDownload = schematicFormatService.convert(originalFile, format);
                    isTempFile = true;
                } catch (IllegalArgumentException e) {
                    // 格式不支持
                    return ResponseEntity.badRequest().body(null);
                } catch (ConversionException e) {
                    // 转换内部错误
                    System.err.println("Conversion failed: " + e.getMessage());
                    return ResponseEntity.internalServerError().build();
                }
            }

            // 3. 构造下载文件名
            // 如果是转换后的文件，必须改变后缀名
            // 原始文件名: "house.schem" -> 转换后: "house.litematic"
            String originalName = originalFile.getName();
            String downloadFilename;

            if (isTempFile) {
                // 剥离原后缀，加上新后缀
                String nameWithoutExt = originalName.contains(".")
                        ? originalName.substring(0, originalName.lastIndexOf('.'))
                        : originalName;
                // 注意：这里简单加个点。如果 format 是 "nbt"，后缀就是 ".nbt"
                downloadFilename = nameWithoutExt + "." + requestedFormat.getExtension().replace(".", "");
            } else {
                downloadFilename = originalName;
            }

            // 4. 构造 Resource
            // 这里的 FileCleanupResource 是一个建议的优化（见下方说明），或者直接用 FileSystemResource
            Resource resource = new FileSystemResource(fileToDownload);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"")
                    .body(resource);

        } catch (FileNotFoundException e) {
            System.out.println("downloadSchematic, File not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * [修改后] 获取模板缩略图接口
     * URL: GET /api/v1/templates/{id}/thumbnail
     * * 逻辑变更：只读模式。如果图片不存在，直接返回 404，不触发后端生成。
     * 图片的生成现在完全由插件侧 (PluginSnapshotManager) 在提交时负责。
     */
    @AuthRequired
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<?> getThumbnail(
            UserContext user,
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "0") Integer angle
    ) {
        try {
            // 1. 调用 Service 查找文件
            File thumbnailFile = templateService.getThumbnailFile(id, angle);

            // 2. 包装为 Resource
            Resource resource = new FileSystemResource(thumbnailFile);

            // 3. 返回图片流
            return ResponseEntity.ok()
                    // 设置缓存：因为文件名带版本号，内容是不可变的，可以设置较长的缓存时间
                    // 客户端检测到 404 后可以显示默认占位图
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                    .body(resource);

        } catch (FileNotFoundException e) {
            // 预期内的异常：图片还没生成好，或者生成失败了
            // 返回 404 Not Found，同时返回 token 允许上传
            try {
                TemplateThumbnailInfo info = templateService.getThumbnailInfo(id, user);
                String token = jwtUtil.generateThumbnailToken(id, info.getPath(), info.getVersion(), angle);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ThumbnailUploadTokenDto(token));
            } catch (Exception ex) {
                // If info fetch fails (e.g. template not found), just return 404 without token
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            // 预期外的异常 (数据库连接失败、IO错误等)
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/thumbnail")
    public ResponseEntity<Void> uploadThumbnail(
            @RequestBody ThumbnailUploadRequestDto request
    ) {
        String token = request.getToken();
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate Subject to ensure it's a thumbnail upload token
        String subject = jwtUtil.extractUsername(token);
        if (!"thumbnail_upload".equals(subject)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> claims = jwtUtil.extractThumbnailClaims(token);
        String path = (String) claims.get("path");
        String version = (String) claims.get("version");
        Integer angle = (Integer) claims.get("angle");

        if (path == null || version == null || angle == null) {
            return ResponseEntity.badRequest().build();
        }

        // base64 decode
        String cleanBase64 = request.getImage();
        if (cleanBase64.contains(",")) {
            cleanBase64 = cleanBase64.split(",")[1];
        }
        // Remove new lines if any
        cleanBase64 = cleanBase64.replaceAll("\\s", "");

        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

        try {
            templateService.saveThumbnail(path, version, angle, imageBytes);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/move")
    @AuthRequired
    public ResponseEntity<Void> moveTemplate(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            UserContext user
    ) {
        String newPath = body.get("path");
        try {
            templateService.moveTemplate(id, newPath, user);
            return ResponseEntity.ok().build();
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/transfer")
    @AuthRequired
    public ResponseEntity<Void> transferTemplate(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            UserContext user
    ) {
        String newOwner = body.get("newOwner");
        try {
            templateService.transferTemplate(id, newOwner, user);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // User not found
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Template not found")) return ResponseEntity.notFound().build();
            return ResponseEntity.internalServerError().build();
        }
    }

    private TemplateInstanceDto toTemplateInstanceDto(Instance instance) {
        TemplateInstanceDto dto = new TemplateInstanceDto();
        dto.setId(instance.getId());
        dto.setTemplateId(instance.getTemplateId());
        dto.setCurrentVersionId(instance.getCurrentVersionId());
        dto.setWorldId(instance.getWorldId());
        dto.setX(instance.getX());
        dto.setY(instance.getY());
        dto.setZ(instance.getZ());
        dto.setRotationY(instance.getRotationY());
        dto.setFlipX(instance.isFlipX());
        dto.setFlipY(instance.isFlipY());
        dto.setFlipZ(instance.isFlipZ());
        dto.setAutoUpdate(instance.isAutoUpdate());
        dto.setPlacedAt(instance.getPlacedAt());
        dto.setPlacedBy(instance.getPlacedBy());
        dto.setPlacedByName(instance.getPlacedByName());
        dto.setDeletedTimestamp(instance.getDeletedTimestamp());
        dto.setMaskXNeg(instance.getMaskXNeg());
        dto.setMaskXPos(instance.getMaskXPos());
        dto.setMaskYNeg(instance.getMaskYNeg());
        dto.setMaskYPos(instance.getMaskYPos());
        dto.setMaskZNeg(instance.getMaskZNeg());
        dto.setMaskZPos(instance.getMaskZPos());
        dto.setEmbeddedInTemplateId(instance.getEmbeddedInTemplateId());
        dto.setDeleted(instance.isDeleted());
        dto.setWild(instance.isWild());
        return dto;
    }

    private Specification<Instance> buildTemplateInstanceSpecification(UUID templateId, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("templateId"), templateId));

            if (search != null && !search.trim().isEmpty()) {
                String[] conditions = search.split(",");
                for (String condition : conditions) {
                    String[] parts = condition.split(":", 2);
                    if (parts.length != 2) {
                        continue;
                    }

                    String fieldOp = parts[0].trim();
                    String value = parts[1].trim();
                    boolean like = fieldOp.endsWith("~");
                    String field = like ? fieldOp.substring(0, fieldOp.length() - 1).trim() : fieldOp;
                    Path<?> path;
                    try {
                        path = resolvePath(root, field);
                    } catch (IllegalArgumentException ex) {
                        continue;
                    }
                    Object converted = convertValue(path.getJavaType(), value);
                    if (like && path.getJavaType() == String.class) {
                        predicates.add(cb.like(path.as(String.class), "%" + value + "%"));
                    } else {
                        predicates.add(cb.equal(path, converted));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Path<?> resolvePath(Path<?> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object convertValue(Class<?> targetType, String value) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == UUID.class) {
            return UUID.fromString(value);
        } else if (targetType.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetType, value);
        }
        return value;
    }
}
