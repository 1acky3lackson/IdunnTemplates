package com.jackyblackson.idunntemplates.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "idunn_trusted_server", indexes = {
    @Index(name = "idx_trusted_server_token", columnList = "token", unique = true)
})
@Getter
@Setter
public class TrustedServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String remarks;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(nullable = false)
    private Long createdAt;

    @Column(nullable = false, length = 100)
    private String createdByUsername;
}
