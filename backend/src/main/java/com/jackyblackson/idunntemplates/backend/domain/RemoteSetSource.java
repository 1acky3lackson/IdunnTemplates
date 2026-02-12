package com.jackyblackson.idunntemplates.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "idunn_remote_set_sources")
public class RemoteSetSource {

    public enum SourceType {
        PATH,
        REMOTE_SET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "remote_set_id", nullable = false)
    private RemoteSet remoteSet;

    @Column(nullable = false)
    private Double weight = 1.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType type;

    @Column(name = "path_value")
    private String path;

    @ManyToOne
    @JoinColumn(name = "target_remote_set_id")
    private RemoteSet targetSet;

    public RemoteSetSource() {}

    public RemoteSetSource(RemoteSet remoteSet, Double weight, String path) {
        this.remoteSet = remoteSet;
        this.weight = weight;
        this.type = SourceType.PATH;
        this.path = path;
    }

    public RemoteSetSource(RemoteSet remoteSet, Double weight, RemoteSet targetSet) {
        this.remoteSet = remoteSet;
        this.weight = weight;
        this.type = SourceType.REMOTE_SET;
        this.targetSet = targetSet;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RemoteSet getRemoteSet() {
        return remoteSet;
    }

    public void setRemoteSet(RemoteSet remoteSet) {
        this.remoteSet = remoteSet;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public RemoteSet getTargetSet() {
        return targetSet;
    }

    public void setTargetSet(RemoteSet targetSet) {
        this.targetSet = targetSet;
    }
}
