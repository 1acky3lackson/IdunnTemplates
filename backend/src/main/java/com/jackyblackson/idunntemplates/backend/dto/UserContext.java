package com.jackyblackson.idunntemplates.backend.dto;

public class UserContext {
    private String username;
    private String uuid;

    public UserContext() {
    }

    public UserContext(String username, String uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
