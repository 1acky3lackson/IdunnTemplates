package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.core.domain.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
