package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateVersionRepository extends  JpaRepository<TemplateVersion, Integer>,
                                                    JpaSpecificationExecutor<TemplateVersion> {

    // 获取某个模板的所有版本，按创建时间倒序排列 (最新的在前)
    List<TemplateVersion> findByTemplateIdOrderByCreatedAtDesc(UUID templateId);

    // 根据业务 ID (timestamp string) 查找特定版本
    Optional<TemplateVersion> findByTemplateIdAndVersionId(UUID templateId, String versionId);

    // 获取某个模板的最新一个版本
    Optional<TemplateVersion> findFirstByTemplateIdOrderByCreatedAtDesc(UUID templateId);

    // 批量查找多个模板的版本
    List<TemplateVersion> findByTemplateIn(List<Template> templates);

    Optional<TemplateVersion> findTopByTemplateOrderByCreatedAtDesc(Template template);

    List<TemplateVersion> findByCreatedAt(long createAtMsTimestamp);
}