package com.example.demo.dto;

public class UserAuthDTO {
    private final String uuid;
    private final boolean deleted;

    public UserAuthDTO(String uuid, boolean deleted) {
        this.uuid = uuid;
        this.deleted = deleted;
    }
    public String getUuid() { return uuid; }
    public boolean isDeleted() { return deleted; }
}

