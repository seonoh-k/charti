package com.example.demo.dto.request;

import lombok.Getter;

@Getter
public class UserUpdateRequest {

    private String name;         // Firebase displayName
    private String phoneNumber;  // Firebase phoneNumber
    private String nickname;     // DB 전용 필드
    private String password;     // DB 전용 필드
    private String profileImage;     // DB 전용 필드

    private String AddressDTO;     // DB 전용 필드
}
