package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.domain.Tag;
import com.jackyblackson.idunntemplates.backend.dto.TagCreateRequest;
import com.jackyblackson.idunntemplates.backend.service.TagService;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags(@RequestParam(required = false, defaultValue = "false") boolean rootsOnly) {
        if (rootsOnly) {
            return ResponseEntity.ok(tagService.getRootTags());
        }
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody TagCreateRequest request) {
        Tag tag = tagService.createTag(request.getName(), request.getParentId());
        return ResponseEntity.ok(tag);
    }

    @GetMapping("/{id}/templates")
    public ResponseEntity<List<Template>> getTemplatesByTag(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTemplatesByTag(id));
    }

    @PostMapping("/{tagId}/templates/{templateId}")
    public ResponseEntity<Void> addTagToTemplate(@PathVariable Long tagId, @PathVariable UUID templateId) {
        tagService.addTagToTemplate(tagId, templateId);
        return ResponseEntity.ok().build();
    }
}
