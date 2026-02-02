package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.backend.domain.TemplateColorScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateColorSchemeRepository extends JpaRepository<TemplateColorScheme, Long> {
    Optional<TemplateColorScheme> findByTemplateId(UUID templateId);
    List<TemplateColorScheme> findByTemplateIdIn(List<UUID> templateIds);
}
