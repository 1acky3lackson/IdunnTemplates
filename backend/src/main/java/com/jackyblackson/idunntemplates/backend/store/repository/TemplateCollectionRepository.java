package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.backend.domain.TemplateCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateCollectionRepository extends
        JpaRepository<TemplateCollection, Long>,
        JpaSpecificationExecutor<TemplateCollection> {
}
