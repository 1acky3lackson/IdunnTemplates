package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.CrudTablePageResponse;
import com.jackyblackson.idunntemplates.backend.dto.VersionSearchCriteria;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
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
    @AuthRequired
    @GetMapping("/{templateId}/versions")
    public ResponseEntity<CrudTablePageResponse<TemplateVersion>> getTemplateVersions(
            @PathVariable UUID templateId,
            @RequestParam(required = false) String search,
            UserContext user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        VersionSearchCriteria criteria = parseSearchCriteria(search);
        criteria.setTemplateId(templateId);

        Page<TemplateVersion> page = versionService.searchVersions(criteria, pageable);
        return ResponseEntity.ok(CrudTablePageResponse.from(page));
    }

    private VersionSearchCriteria parseSearchCriteria(String search) {
        VersionSearchCriteria criteria = new VersionSearchCriteria();
        if (search == null || search.trim().isEmpty()) {
            return criteria;
        }

        String[] conditions = search.split(",");
        for (String condition : conditions) {
            String[] parts = condition.split(":", 2);
            if (parts.length != 2) {
                continue;
            }

            String fieldOp = parts[0].trim();
            String value = parts[1].trim();
            boolean fuzzy = fieldOp.endsWith("~");
            String field = fuzzy ? fieldOp.substring(0, fieldOp.length() - 1).trim() : fieldOp;

            switch (field) {
                case "versionId" -> criteria.setVersionId(value);
                case "submitterId" -> criteria.setSubmitterId(UUID.fromString(value));
                case "message" -> criteria.setMessageKeyword(value);
                case "createdAt" -> {
                    Long createdAt = Long.parseLong(value);
                    criteria.setMinCreatedAt(createdAt);
                    criteria.setMaxCreatedAt(createdAt);
                }
                case "minCreatedAt" -> criteria.setMinCreatedAt(Long.parseLong(value));
                case "maxCreatedAt" -> criteria.setMaxCreatedAt(Long.parseLong(value));
                default -> {
                }
            }
        }
        return criteria;
    }
}
