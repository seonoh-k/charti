package com.example.demo.fcm.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.fcm.repository.FcmSendHistoryRepository;
import com.example.demo.fcm.repository.UserFcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserFcmTokenRepository tokenRepository;
    private final FcmSender fcmSender;
    private final FcmSendHistoryRepository historyRepository;

    /**
     * 조건에 맞는 사용자들에게 FCM 알림을 발송하고, 발송 이력을 기록한다.
     *
     * @param title      알림 제목
     * @param body       알림 본문
     * @param category   알림 카테고리 (DAILY, SPECIAL 등)
     * @param ageGroup   수신 대상 자녀의 연령대 (null이면 전체 대상)
     * @return 성공적으로 알림을 수신한 사용자 수
     */
    public int sendNotificationToTarget(String title, String body,
                                        FcmCategory category, AgeGroup ageGroup) {

        // 🔍 대상 토큰 필터링: 삭제된 사용자 제외 + ageGroup 조건 필터
        List<UserFcmToken> targets = tokenRepository.findAll().stream()
                .filter(token -> !token.getUser().isDeleted()) // 탈퇴하지 않은 사용자
                .filter(token -> ageGroup == null || (
                        token.getUser().getMember() != null &&
                                token.getUser().getMember().getChildren().stream()
                                        .anyMatch(child -> child.getAgeGroup() == ageGroup)
                ))
                .toList();

        // 📤 알림 발송
        int successCount = 0;
        for (UserFcmToken target : targets) {
            boolean sent = fcmSender.send(target.getFcmToken(), title, body);
            if (sent) successCount++;
        }

        // 📝 발송 이력 저장
        FcmSendHistory history = FcmSendHistory.builder()
                .title(title)
                .body(body)
                .category(category != null ? category : FcmCategory.NOTICE)
                .targetCondition("ageGroup=" + (ageGroup != null ? ageGroup.name() : "ALL"))
                .targetCount(targets.size())
                .successCount(successCount)
                .sentAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);

        return successCount;
    }
}
