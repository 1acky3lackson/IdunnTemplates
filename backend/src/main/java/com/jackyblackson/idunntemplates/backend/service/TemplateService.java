package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.backend.store.spec.TemplateSpecifications;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    @Deprecated
    @Transactional(readOnly = true)
    public List<Template> getTemplatesByPath(String path) {
        return templateRepository.findByPathStartingWith(path);
    }

    /**
     * 复合条件搜索 + 分页 + 排序
     */
    @Transactional(readOnly = true)
    public Page<Template> searchTemplates(TemplateSearchCriteria criteria, Pageable pageable) {
        // 将 DTO 转换为 Specification
        var spec = TemplateSpecifications.withCriteria(criteria);
        // 执行查询
        return templateRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Template> getTemplateById(UUID id) {
        return templateRepository.findById(id);
    }
}
