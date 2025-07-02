package com.example.demo.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

/**
 * FCM 발송 결과를 담는 DTO
 */
@Getter
@AllArgsConstructor
public class FcmSendResultDto {
    private int totalTargetCount; // 총 발송 대상자 수
    private int successCount;     // 실제 발송 성공 수
    private List<String> successfulRecipientNames; // 성공한 자녀 이름 목록
    private List<String> failedRecipientNames; // 실패한 자녀 이름 목록
}
