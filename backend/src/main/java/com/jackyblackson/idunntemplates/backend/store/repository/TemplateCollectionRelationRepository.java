package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.backend.domain.TemplateCollectionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateCollectionRelationRepository extends JpaRepository<TemplateCollectionRelation, Long> {

    boolean existsByCollectionIdAndTemplateId(Long collectionId, UUID templateId);

    Optional<TemplateCollectionRelation> findByCollectionIdAndTemplateId(Long collectionId, UUID templateId);

    void deleteByCollectionId(Long collectionId);
}
