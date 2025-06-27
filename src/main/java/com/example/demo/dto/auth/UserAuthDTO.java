package com.example.demo.dto.auth;

public class UserAuthDTO implements AuthDTO{
    private final String uuid;
    private final boolean deleted;

    public UserAuthDTO(String uuid, boolean deleted) {
        this.uuid = uuid;
        this.deleted = deleted;
    }
    public String getUuid() { return uuid; }
    public boolean isDeleted() { return deleted; }
}

