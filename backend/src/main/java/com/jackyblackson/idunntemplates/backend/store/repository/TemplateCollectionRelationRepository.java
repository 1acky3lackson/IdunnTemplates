package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.backend.domain.TemplateCollectionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateCollectionRelationRepository extends JpaRepository<TemplateCollectionRelation, Long> {

    boolean existsByCollectionIdAndTemplateId(Long collectionId, UUID templateId);

    Optional<TemplateCollectionRelation> findByCollectionIdAndTemplateId(Long collectionId, UUID templateId);

    void deleteByCollectionId(Long collectionId);

    @Query("SELECT r.template.id FROM TemplateCollectionRelation r WHERE r.collection.id = :collectionId")
    List<UUID> findTemplateIdsByCollectionId(@Param("collectionId") Long collectionId);
}
