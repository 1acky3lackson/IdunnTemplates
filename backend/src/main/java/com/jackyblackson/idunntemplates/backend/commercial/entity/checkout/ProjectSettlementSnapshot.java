package com.jackyblackson.idunntemplates.backend.commercial.entity.checkout;

import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "commercial_project_settlement_snapshot",
        uniqueConstraints = @UniqueConstraint(name = "uniq_project_snapshot_project_id", columnNames = "project_id"),
        indexes = {
                @Index(name = "idx_project_snapshot_project_id", columnList = "project_id"),
                @Index(name = "idx_project_snapshot_scanned_at", columnList = "scanned_at_ms")
        })
public class ProjectSettlementSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "project_effective_blocks", nullable = false)
    private Long projectEffectiveBlocks = 0L;

    @Column(name = "scanned_at_ms", nullable = false)
    private Long scannedAtMs = System.currentTimeMillis();

    @Column(name = "source_server_name", length = 128)
    private String sourceServerName;

    @Column(name = "payload_json", columnDefinition = "text", nullable = false)
    private String payloadJson;

    @Column(name = "update_time_ms", nullable = false)
    private Long updateTimeMs = System.currentTimeMillis();
}
