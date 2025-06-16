package com.example.demo.fcm.service;

import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.fcm.repository.FcmSendHistoryRepository;
import com.example.demo.fcm.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmSender {

    private final FirebaseMessaging firebaseMessaging;
    private final UserFcmTokenRepository tokenRepository;
    private final FcmSendHistoryRepository historyRepository;

    /**
     * 전체 대상에게 FCM 알림을 발송하고 발송 이력을 저장합니다.
     *
     * @param title    알림 제목
     * @param body     알림 본문 내용
     * @param category 알림 분류 (예: NOTICE, SURVEY 등)
     * @param conditionDescription 발송 조건 설명 (예: "전체 대상", "ageGroup=1~2세" 등)
     */
    public void sendToAll(String title, String body, FcmCategory category, String conditionDescription) {
        // 삭제되지 않은 사용자들의 활성 토큰만 가져오기
        List<UserFcmToken> tokens = tokenRepository.findAll().stream()
                .filter(token -> !token.getUser().isDeleted())
                .filter(UserFcmToken::isActive)
                .toList();

        int success = 0;

        for (UserFcmToken token : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(token.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();

                firebaseMessaging.send(message); // Firebase로 발송
                success++;
            } catch (Exception e) {
                System.err.println("❌ FCM 전송 실패 - token: " + token.getFcmToken() + " / 이유: " + e.getMessage());
            }
        }

        // 발송 이력 저장
        FcmSendHistory history = FcmSendHistory.builder()
                .title(title)
                .body(body)
                .category(category)
                .targetCondition(conditionDescription)
                .targetCount(tokens.size())
                .successCount(success)
                .sentAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }

    public boolean send(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            firebaseMessaging.send(message);  // 전송
            return true;  // 성공
        } catch (Exception e) {
            System.err.println("❌ FCM 전송 실패: " + e.getMessage());
            return false;  // 실패
        }
    }

}
