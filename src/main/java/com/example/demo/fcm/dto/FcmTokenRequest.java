package com.example.demo.fcm.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 클라이언트가 FCM 토큰을 전달할 때 사용할 요청 DTO
 */
@Getter
@Setter
public class FcmTokenRequest {
    private String token;
}