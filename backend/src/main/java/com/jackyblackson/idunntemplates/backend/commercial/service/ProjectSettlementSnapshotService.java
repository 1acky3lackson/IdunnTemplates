package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.ProjectSettlementSnapshotPayload;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.ProjectSettlementSnapshot;
import com.jackyblackson.idunntemplates.backend.commercial.repository.ProjectRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.ProjectSettlementSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectSettlementSnapshotService {

    private final ProjectRepository projectRepository;
    private final ProjectSettlementSnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProjectSettlementSnapshot saveSnapshot(Long projectId, ProjectSettlementSnapshotPayload payload) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        ProjectSettlementSnapshot snapshot = snapshotRepository.findByProject(project)
                .orElseGet(ProjectSettlementSnapshot::new);
        snapshot.setProject(project);
        snapshot.setProjectEffectiveBlocks(payload.getProjectEffectiveBlocks() == null ? 0L : payload.getProjectEffectiveBlocks());
        snapshot.setScannedAtMs(payload.getScannedAtMs() == null ? System.currentTimeMillis() : payload.getScannedAtMs());
        snapshot.setSourceServerName(payload.getSourceServerName());
        snapshot.setPayloadJson(writePayload(payload));
        snapshot.setUpdateTimeMs(System.currentTimeMillis());
        return snapshotRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectSettlementSnapshotPayload> getPayloadByProjectId(Long projectId) {
        return snapshotRepository.findByProject_Id(projectId)
                .map(ProjectSettlementSnapshot::getPayloadJson)
                .map(this::readPayload);
    }

    public ProjectSettlementSnapshotPayload readPayload(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, ProjectSettlementSnapshotPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse project settlement snapshot payload", e);
        }
    }

    private String writePayload(ProjectSettlementSnapshotPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize project settlement snapshot payload", e);
        }
    }
}
