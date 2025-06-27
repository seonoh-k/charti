package com.example.demo.dto.auth;

import lombok.*;

@Getter
@Setter
public class AdminAuthDTO implements AuthDTO{

    private final String uuid;
    private final boolean deleted;

    public AdminAuthDTO(String uuid, boolean deleted) {
        this.uuid = uuid;
        this.deleted = deleted;
    }
    public String getUuid() { return uuid; }
    public boolean isDeleted() { return deleted; }
}
