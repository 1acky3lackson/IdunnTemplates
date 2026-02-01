package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.service.TemplateService;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<Template>> getTemplates(@RequestParam(required = false, defaultValue = "") String path) {
        return ResponseEntity.ok(templateService.getTemplatesByPath(path));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplate(@PathVariable UUID id) {
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
