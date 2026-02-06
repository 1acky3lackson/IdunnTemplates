package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.backend.dto.TemplateWithColorsDto;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.SchematicFormatService;
import com.jackyblackson.idunntemplates.backend.service.TemplateColorService;
import com.jackyblackson.idunntemplates.backend.service.TemplateService;
import com.jackyblackson.idunntemplates.backend.util.CollectionUtils;
import com.jackyblackson.idunntemplates.backend.util.JwtUtil;
import com.jackyblackson.idunntemplates.backend.dto.TemplateThumbnailInfo;
import com.jackyblackson.idunntemplates.backend.dto.ThumbnailUploadRequestDto;
import com.jackyblackson.idunntemplates.backend.dto.ThumbnailUploadTokenDto;
import com.jackyblackson.idunntemplates.core.domain.Template;
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
import pitheguy.schemconvert.converter.ConversionException;
import pitheguy.schemconvert.converter.formats.SchemSchematicFormat;
import pitheguy.schemconvert.converter.formats.SchematicFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateColorService templateColorService;

    private final SchematicFormatService schematicFormatService;
    private final JwtUtil jwtUtil;

    @Autowired
    public TemplateController(TemplateService templateService, TemplateColorService templateColorService, SchematicFormatService schematicFormatService, JwtUtil jwtUtil) {
        this.templateService = templateService;
        this.templateColorService = templateColorService;
        this.schematicFormatService = schematicFormatService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 搜索接口
     * 示例 URL: GET /api/v1/templates?pathPrefix=users/&minWidth=10&locked=true&page=0&size=10&sort=metadata.creationTime,desc
     */
    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<TemplateWithColorsDto>> searchTemplates(
            // 自动绑定 url 参数到 criteria 对象
            @ModelAttribute TemplateSearchCriteria criteria,
            UserContext userContext,
            // 自动处理分页和排序参数 (默认每页 20 条，按路径升序)
            @PageableDefault(size = 20, sort = "path", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Template> page = templateService.searchTemplates(criteria, pageable, userContext);
        Map<UUID, List<String>> colors = templateColorService.resolveColorsForTemplates(page.getContent());

        // 假设 colors 是之前 resolveColorsForTemplates 得到的结果 Map
        List<String> defaultColorList = Collections.emptyList();

        List<TemplateWithColorsDto> dtos = CollectionUtils.mapToList(
                page.getContent(),
                Template::getId,
                colors,
                TemplateWithColorsDto::new, // 构造函数引用：(template, colorList) -> new Dto
                defaultColorList
        );

        return ResponseEntity.ok(new PageImpl<>(
                dtos,
                page.getPageable(),
                page.getTotalElements()
        ));
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
}
