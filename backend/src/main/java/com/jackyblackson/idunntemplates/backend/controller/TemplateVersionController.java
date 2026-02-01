package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.dto.VersionSearchCriteria;
import com.jackyblackson.idunntemplates.backend.service.TemplateVersionService;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateVersionController {

    private final TemplateVersionService versionService;

    @Autowired
    public TemplateVersionController(TemplateVersionService versionService) {
        this.versionService = versionService;
    }

    /**
     * 获取指定模板的版本列表
     * URL: GET /api/v1/templates/{templateId}/versions
     */
    @GetMapping("/{templateId}/versions")
    public ResponseEntity<Page<TemplateVersion>> getTemplateVersions(
            // 1. 从 URL 路径中获取 Template ID
            @PathVariable UUID templateId,

            // 2. 从 Query Params 绑定其他筛选条件 (submitter, message, time...)
            @ModelAttribute VersionSearchCriteria criteria,

            // 3. 分页与排序 (默认按创建时间倒序)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // [关键安全步骤]
        // 强制将 criteria 中的 templateId 设置为 URL 中的 ID。
        // 这防止了用户访问 "/templates/A/versions" 却在参数里传 "templateId=B" 导致的数据泄露。
        criteria.setTemplateId(templateId);

        return ResponseEntity.ok(versionService.searchVersions(criteria, pageable));
    }
}