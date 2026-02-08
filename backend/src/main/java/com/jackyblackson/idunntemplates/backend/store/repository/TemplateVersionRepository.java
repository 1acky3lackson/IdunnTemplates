package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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

    /**
     * 使用 JPQL (ORM 抽象查询语言)
     * 这里的 'TemplateVersion' 是 Java 类名，'submitterId' 是 Java 属性名。
     * Hibernate 会根据底层数据库方言自动生成对应的 DISTINCT SQL。
     */
    @Query("SELECT DISTINCT tv.submitterId FROM TemplateVersion tv WHERE tv.submitterId IS NOT NULL")
    List<UUID> findDistinctSubmitterIds();
}