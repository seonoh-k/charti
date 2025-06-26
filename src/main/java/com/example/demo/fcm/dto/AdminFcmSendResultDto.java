package com.example.demo.fcm.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자용 FCM 발송 결과를 담는 DTO
 */
@Getter
@Builder
public class AdminFcmSendResultDto {
    private final String targetType; // 발송 대상의 종류 (예: "ALL", "RISK", "GROUP")
    private final int totalTargetCount;
    private final int successCount;

    // 특정 그룹 발송 시에만 채워질 상세 명단
    @Builder.Default
    private final List<String> successfulRecipientNames = new ArrayList<>();
    @Builder.Default
    private final List<String> failedRecipientNames = new ArrayList<>();
}