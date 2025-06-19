package com.example.demo.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    // 원래비밀번호
    private String currentPassword;
    // 새비밀번호
    private String newPassword;
    // 새비밀번호 확인
    private String confirmPassword;
}
