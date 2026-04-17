package com.jackyblackson.idunntemplates.backend.store.repository;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InstanceRepository extends JpaRepository<Instance, String>, JpaSpecificationExecutor<Instance> {
    List<Instance> findByTemplateId(UUID templateId);
}
