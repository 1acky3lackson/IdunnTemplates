package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;

    @Autowired
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * 搜索接口
     * 示例 URL: GET /api/v1/templates?pathPrefix=users/&minWidth=10&locked=true&page=0&size=10&sort=metadata.creationTime,desc
     */
    @GetMapping
    public ResponseEntity<Page<Template>> searchTemplates(
            // 自动绑定 url 参数到 criteria 对象
            @ModelAttribute TemplateSearchCriteria criteria,

            // 自动处理分页和排序参数 (默认每页 20 条，按路径升序)
            @PageableDefault(size = 20, sort = "path", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(templateService.searchTemplates(criteria, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplate(@PathVariable UUID id) {
        return templateService.getTemplateById(id)
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
     * [新增] 获取模板缩略图接口
     * URL: <img src="/api/v1/templates/{id}/thumbnail" />
     */
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "false") Boolean refresh,
            @RequestParam(required = false, defaultValue = "0") Integer angle
    ) {
        try {
            // 1. 调用 Service 获取文件 (内部包含版本检查、清理旧图、自动生成的逻辑)
            File thumbnailFile = templateService.getOrGenerateThumbnail(id, refresh != null && refresh, angle);

            // 2. 包装为 Resource
            Resource resource = new FileSystemResource(thumbnailFile);

            return ResponseEntity.ok()
                    // 3. 设置 Content-Type 为 image/png，这样浏览器才能直接渲染
                    .contentType(MediaType.IMAGE_PNG)
                    // 4. 设置浏览器缓存
                    // 由于文件名包含版本号(thumbnail_123456.png)，我们可以设置较长的缓存时间
                    // 当版本更新时，版本号变了，生成的文件名变了，自然会失效
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                    .body(resource);

        } catch (FileNotFoundException e) {
            // 404: 模板不存在或生成失败
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
