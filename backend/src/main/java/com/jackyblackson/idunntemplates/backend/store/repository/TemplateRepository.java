package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemplateRepository extends
        JpaRepository<Template, UUID>,
        JpaSpecificationExecutor<Template> {
    List<Template> findByPathStartingWith(String path);

    boolean existsByPath(String path);

    Page<Template> findByPathStartingWith(String path, Pageable pageable);

    List<Template> findByLastVersionAtIsNull();

    // 新增：根据 Template 获取所有关联的 NeteaseProduct
    @Query("SELECT p FROM NeteaseProduct p JOIN p.templates t WHERE t.id = :templateId")
    List<NeteaseProduct> findNeteaseProductsByTemplateId(@Param("templateId") UUID templateId);

    // 若需要直接传入 Template 对象
    @Query("SELECT p FROM NeteaseProduct p JOIN p.templates t WHERE t = :template")
    List<NeteaseProduct> findNeteaseProductsByTemplate(@Param("template") Template template);
}
