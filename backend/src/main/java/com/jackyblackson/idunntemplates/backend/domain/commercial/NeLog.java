package com.jackyblackson.idunntemplates.backend.domain.commercial;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ne_logs")
public class NeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "create_time_ms", nullable = false)
    private Long createTimeMs;

    @Column(name = "start_crawl_time_ms")
    private Long startCrawlTimeMs;

    @Column(name = "end_crawl_time_ms")
    private Long endCrawlTimeMs;

}
