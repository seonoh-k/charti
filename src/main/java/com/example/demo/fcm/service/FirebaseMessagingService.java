package com.example.demo.fcm.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FirebaseMessagingService {

    /**
     * 단일 FCM 토큰에 메시지 전송
     *
     * @param token 수신자 FCM 토큰
     * @param title 알림 제목
     * @param body  알림 내용
     * @param link  클릭 시 이동할 URL
     */
    public void sendMessageToToken(String token, String title, String body, String link) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("link", link)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("✅ FCM 전송 성공: response = {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("❌ FCM 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("FCM 메시지 전송 실패", e);
        }
    }
}
