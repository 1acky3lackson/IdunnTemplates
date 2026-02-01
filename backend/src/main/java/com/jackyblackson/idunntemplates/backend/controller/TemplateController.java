package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.backend.service.TemplateService;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;

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
}
