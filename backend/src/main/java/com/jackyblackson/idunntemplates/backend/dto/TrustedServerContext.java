package com.jackyblackson.idunntemplates.backend.dto;

public class TrustedServerContext {
    private Long serverId;
    private String serverName;

    public TrustedServerContext() {
    }

    public TrustedServerContext(Long serverId, String serverName) {
        this.serverId = serverId;
        this.serverName = serverName;
    }

    public Long getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }
}
