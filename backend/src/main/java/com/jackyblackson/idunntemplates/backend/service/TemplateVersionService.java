package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.VersionSearchCriteria;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateVersionRepository;
import com.jackyblackson.idunntemplates.backend.store.spec.TemplateVersionSpecifications;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TemplateVersionService {

    private final TemplateVersionRepository versionRepository;

    @Autowired
    public TemplateVersionService(TemplateVersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    /**
     * [新增] 复杂搜索版本
     */
    @Transactional(readOnly = true)
    public Page<TemplateVersion> searchVersions(VersionSearchCriteria criteria, Pageable pageable) {
        var spec = TemplateVersionSpecifications.withCriteria(criteria);
        return versionRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<TemplateVersion> getVersionsByTemplateId(UUID templateId) {
        return versionRepository.findByTemplateIdOrderByCreatedAtDesc(templateId);
    }

    @Transactional(readOnly = true)
    public Optional<TemplateVersion> getVersion(UUID templateId, String versionId) {
        return versionRepository.findByTemplateIdAndVersionId(templateId, versionId);
    }

    @Transactional(readOnly = true)
    public Optional<TemplateVersion> getLatestVersion(UUID templateId) {
        return versionRepository.findFirstByTemplateIdOrderByCreatedAtDesc(templateId);
    }
}