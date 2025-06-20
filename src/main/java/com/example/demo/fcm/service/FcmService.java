package com.example.demo.fcm.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.entity.Notice;
import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.fcm.repository.FcmSendHistoryRepository;
import com.example.demo.fcm.repository.NoticeRepository;
import com.example.demo.fcm.repository.UserFcmTokenRepository;
import com.example.demo.users.entity.Users;
import jakarta.transaction.Transactional;
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
    private final NoticeRepository noticeRepository;

    /**
     * 조건에 맞는 사용자들에게 FCM 알림을 발송하고, 발송 이력을 기록한다.
     *
     * @param title      알림 제목
     * @param body       알림 본문
     * @param category   알림 카테고리 (DAILY, SPECIAL 등)
     * @param ageGroup   수신 대상 자녀의 연령대 (null이면 전체 대상)
     * @return 성공적으로 알림을 수신한 사용자 수
     */
    @Transactional
    public int sendNotificationToTarget(String title,
                                        String body,
                                        FcmCategory category,
                                        AgeGroup ageGroup) {

        List<UserFcmToken> targets = tokenRepository.findAll().stream()
                .filter(token -> !token.getUser().isDeleted())
                .filter(token -> ageGroup == null || (
                        token.getUser().getMember() != null &&
                                token.getUser().getMember().getChildren().stream()
                                        .anyMatch(child -> child.getAgeGroup() == ageGroup)
                ))
                .toList();

        int successCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (UserFcmToken target : targets) {
            boolean sent = fcmSender.send(target.getFcmToken(), title, body);
            if (sent) {
                successCount++;

                // ── 여기서 Notice 저장 ──
                Notice notice = Notice.builder()
                        .user(target.getUser())
                        .title(title)
                        .body(body)
                        .category(category != null ? category : FcmCategory.NOTICE)
                        .sentAt(now)
                        .build();
                noticeRepository.save(notice);
            }
        }

        // 발송 이력 저장
        historyRepository.save(
                FcmSendHistory.builder()
                        .title(title)
                        .body(body)
                        .category(category != null ? category : FcmCategory.NOTICE)
                        .targetCondition("ageGroup=" + (ageGroup != null ? ageGroup.name() : "ALL"))
                        .targetCount(targets.size())
                        .successCount(successCount)
                        .sentAt(now)
                        .build()
        );

        return successCount;
    }

    /**
     * 특정 사용자에게만 FCM + Notice + SendHistory 저장
     */
    @Transactional
    public int sendNotificationToUser(Users user,
                                      String title,
                                      String body,
                                      FcmCategory category,
                                      String url) {
        // 1) 해당 유저의 활성화된 토큰만 조회
        List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(user);

        int successCount = 0;
        LocalDateTime now = LocalDateTime.now();

        // 2) FCM 발송 & Notice 저장
        for (UserFcmToken token : tokens) {
            boolean sent = fcmSender.send(token.getFcmToken(), title, body);
            if (sent) {
                successCount++;
                noticeRepository.save(
                        Notice.builder()
                                .user(user)
                                .title(title)
                                .body(body)
                                .category(category != null ? category : FcmCategory.DAILY)
                                .sentAt(now)
                                .url(url)
                                .build()
                );
            }
        }

        // 3) SendHistory 저장 (사용자별 기록)
        historyRepository.save(
                FcmSendHistory.builder()
                        .title(title)
                        .body(body)
                        .category(category)
                        .targetCondition("userId=" + user.getId())
                        .targetCount(tokens.size())
                        .successCount(successCount)
                        .sentAt(now)
                        .build()
        );

        return successCount;
    }

}
