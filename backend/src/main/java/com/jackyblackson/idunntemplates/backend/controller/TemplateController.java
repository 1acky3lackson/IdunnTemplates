package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.backend.dto.TemplateWithColorsDto;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.TemplateColorService;
import com.jackyblackson.idunntemplates.backend.service.TemplateService;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

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

    @Autowired
    public TemplateController(TemplateService templateService, TemplateColorService templateColorService) {
        this.templateService = templateService;
        this.templateColorService = templateColorService;
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

        List<TemplateWithColorsDto> dtos = page.getContent().stream()
                .map(t -> new TemplateWithColorsDto(t, colors.getOrDefault(t.getId(), Collections.emptyList())))
                .toList();

        return ResponseEntity.ok(new PageImpl<>(
                dtos,
                page.getPageable(),
                page.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    @AuthRequired
    public ResponseEntity<TemplateWithColorsDto> getTemplate(@PathVariable UUID id) {
        return templateService.getTemplateById(id)
                .map(t -> {
                    Map<UUID, List<String>> colors = templateColorService.resolveColorsForTemplates(Collections.singletonList(t));
                    return new TemplateWithColorsDto(t, colors.getOrDefault(t.getId(), Collections.emptyList()));
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 下载 Template Schematic 文件
     * URL 示例:
     * 1. /api/v1/templates/{id}/download (下载最新版)
     * 2. /api/v1/templates/{id}/download?version=1706781234000 (下载指定版)
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadSchematic(
            @PathVariable UUID id,
            @RequestParam(required = false) String version
    ) {
        try {
            File file = templateService.getSchematicFile(id, version);
            Resource resource = new FileSystemResource(file);

            // 构造下载文件名: templateName_version.schem
            // 为了文件名安全，只保留字母数字
            String filename = file.getName();

            // 如果你想让下载的文件名更友好（包含模板名），需要再查一次 templateName
            // 但为了性能，直接用磁盘上的 versionId.schem 也可以，或者如下处理：
            // String friendlyName = template.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" + file.getName();

            return ResponseEntity.ok()
                    // 二进制流类型
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    // 强制浏览器弹出下载框
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (FileNotFoundException e) {
            System.out.println("   downloadSchematic, Schem file not found, message: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * [修改后] 获取模板缩略图接口
     * URL: GET /api/v1/templates/{id}/thumbnail
     * * 逻辑变更：只读模式。如果图片不存在，直接返回 404，不触发后端生成。
     * 图片的生成现在完全由插件侧 (PluginSnapshotManager) 在提交时负责。
     */
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(
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
                    .contentType(MediaType.IMAGE_PNG)
                    // 设置缓存：因为文件名带版本号，内容是不可变的，可以设置较长的缓存时间
                    // 客户端检测到 404 后可以显示默认占位图
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                    .body(resource);

        } catch (FileNotFoundException e) {
            // 预期内的异常：图片还没生成好，或者生成失败了
            // 返回 404 Not Found，前端应该展示“暂无预览”或“加载中”的占位图
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            // 预期外的异常 (数据库连接失败、IO错误等)
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
