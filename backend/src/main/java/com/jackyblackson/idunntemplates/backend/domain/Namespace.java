package com.jackyblackson.idunntemplates.backend.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "idunn_namespaces")
public class Namespace {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "owner_uuid")
    private UUID ownerUuid;

    public Namespace() {}

    public Namespace(String name, UUID ownerUuid) {
        this.name = name;
        this.ownerUuid = ownerUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }
}
