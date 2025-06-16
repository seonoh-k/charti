package com.example.demo.dto.info;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Pattern(
            regexp = "^\\S{4,30}$",
            message = "4~30자, 공백 없이 입력해주세요."
    )
    private String username;
    private String uuid;
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.{8,16}$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)\\S+$",
            message = "비밀번호는 8~16자, 대문자·소문자·특수문자 각각 1개 이상 포함해야 하며, 공백은 불가합니다."
    )
    private String password;
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Pattern(
            regexp = "^[가-힣A-Za-z0-9_-]{2,8}$",
            message = "닉네임은 2~8자: 한글·영문·숫자·_,- 만 가능합니다. 공백 금지."
    )
    private String nickname;
    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Pattern(
            regexp = "^[가-힣]{2,8}$",
            message = "이름은 2~8자 한글만 가능합니다. 공백은 불가합니다."
    )
    private String name;
    private String role;
    private String phoneNumber;
    private String provider;
    private String providerId;
    private String profileImage;
    // sms 인증시 발급받는 토큰
    private String smsIdToken;

}
