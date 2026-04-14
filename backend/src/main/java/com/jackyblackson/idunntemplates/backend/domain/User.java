package com.jackyblackson.idunntemplates.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "idunn_users", indexes={
        @Index(name = "idx_users_unique_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_users_unique_username", columnList = "username", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(name = "uuid", length = 36, nullable = false)

    private String uuid;

    @Setter
    @Getter
    @Column(name = "username", nullable = false)
    private String username;

    @Setter
    @Getter
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Setter
    @Getter
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors, Getters, Setters
    public User() {}

    public User(String uuid, String username, String passwordHash) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

}
