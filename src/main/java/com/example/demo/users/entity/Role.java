package com.example.demo.users.entity;


import lombok.Getter;

@Getter
public enum Role {

    ROLE_MEMBER("ROLE_MEMBER"),
    ROLE_EXPERT("ROLE_EXPERT"),
    ROLE_MANAGER("ROLE_MANAGER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private String key;

    Role(String key) {
        this.key = key;
    }

}

