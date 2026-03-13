package com.jackyblackson.idunntemplates.backend.commercial.entity.crawler;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ne_user_logs")
public class NeUserLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ne_log_id")
    private Long neLogId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "pe_products_payload", columnDefinition = "text")
    private String peProductsPayload;

    @Column(name = "pe_product_comment_logs_payload", columnDefinition = "text")
    private String peProductCommentLogsPayload;

    @Column(name = "pe_product_feedback_logs_payload", columnDefinition = "text")
    private String peProductFeedbackLogsPayload;

    @Column(name = "comp_product_logs_payload", columnDefinition = "text")
    private String compProductLogsPayload;

}
