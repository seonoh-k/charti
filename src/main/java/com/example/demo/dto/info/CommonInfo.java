package com.example.demo.dto.info;

import lombok.*;

//{
//        "user": { "username": "...", ... },
//        "expert": { "major": "...", "license": "..." },
//        "address": { "zipNum": "...", "sido": "...", ... }
//}

@Data
public class CommonInfo {
    // 공통
    private Long id;
    // 이메일 입니다.
    private String username;
    private String uuid;
    private String password;
    private String nickname;
    private String name;
    private String role;
    private String phoneNumber;
    private String provider;
    private String providerId;
    private String profileImage;

}
