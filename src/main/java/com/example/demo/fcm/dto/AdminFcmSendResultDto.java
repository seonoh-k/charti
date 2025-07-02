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

    @Builder.Default
    private final List<RecipientResultDto> successfulRecipients  = new ArrayList<>();
    @Builder.Default
    private final List<RecipientResultDto> failedRecipients  = new ArrayList<>();

    public static AdminFcmSendResultDto createEmptyResult(String targetType) {
        return AdminFcmSendResultDto.builder()
                .targetType(targetType)
                .totalTargetCount(0)
                .successCount(0)
                .build();
    }
}