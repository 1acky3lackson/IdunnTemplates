package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.domain.Tag;
import com.jackyblackson.idunntemplates.backend.store.repository.TagRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TemplateRepository templateRepository;

    @Autowired
    public TagService(TagRepository tagRepository, TemplateRepository templateRepository) {
        this.tagRepository = tagRepository;
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tag> getRootTags() {
        return tagRepository.findByParentIsNull();
    }

    @Transactional
    public Tag createTag(String name, Long parentId) {
        Tag tag = new Tag(name);
        if (parentId != null) {
            Tag parent = tagRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent tag not found: " + parentId));
            tag.setParent(parent);
        }
        return tagRepository.save(tag);
    }

    @Transactional
    public void addTagToTemplate(Long tagId, UUID templateId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        tag.getTemplates().add(template);
        tagRepository.save(tag);
    }

    @Transactional(readOnly = true)
    public List<Template> getTemplatesByTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        Set<Template> templates = new HashSet<>();
        collectTemplatesRecursively(tag, templates);
        return new ArrayList<>(templates);
    }

    private void collectTemplatesRecursively(Tag tag, Set<Template> templates) {
        // Initialize lazy collection if needed (Transactional handles session)
        templates.addAll(tag.getTemplates());

        for (Tag child : tag.getChildren()) {
            collectTemplatesRecursively(child, templates);
        }
    }
}
