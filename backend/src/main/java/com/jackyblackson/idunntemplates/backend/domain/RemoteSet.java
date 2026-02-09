package com.jackyblackson.idunntemplates.backend.domain;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "idunn_remote_sets")
public class RemoteSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "creator_uuid", nullable = false)
    private UUID creatorUuid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "namespace_name", nullable = false)
    private Namespace namespace;

    @OneToMany(mappedBy = "remoteSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RemoteSetSource> sources = new ArrayList<>();

    public RemoteSet() {}

    public RemoteSet(String name, UUID creatorUuid, Namespace namespace) {
        this.name = name;
        this.creatorUuid = creatorUuid;
        this.namespace = namespace;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getCreatorUuid() {
        return creatorUuid;
    }

    public void setCreatorUuid(UUID creatorUuid) {
        this.creatorUuid = creatorUuid;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public List<RemoteSetSource> getSources() {
        return sources;
    }

    public void setSources(List<RemoteSetSource> sources) {
        this.sources = sources;
    }
}
