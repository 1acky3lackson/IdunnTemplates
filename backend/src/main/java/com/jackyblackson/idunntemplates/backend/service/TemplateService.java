package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<Template> getTemplatesByPath(String path) {
        return templateRepository.findByPathStartingWith(path);
    }

    @Transactional(readOnly = true)
    public Optional<Template> getTemplateById(UUID id) {
        return templateRepository.findById(id);
    }
}
