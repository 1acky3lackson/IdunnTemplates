package com.jackyblackson.idunntemplates.backend.commercial.repository.chekout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.ProjectSettlementSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectSettlementSnapshotRepository extends JpaRepository<ProjectSettlementSnapshot, Long> {
    Optional<ProjectSettlementSnapshot> findByProject(Project project);
    Optional<ProjectSettlementSnapshot> findByProject_Id(Long projectId);
}
