package com.example.demo.fcm.service;

import com.example.demo.entity.Group;
import com.example.demo.enums.TargetGroup;
import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.entity.Notice;
import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.fcm.repository.FcmSendHistoryRepository;
import com.example.demo.fcm.repository.NoticeRepository;
import com.example.demo.fcm.repository.UserFcmTokenRepository;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Manager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final UserRepository userRepository;
    private final UserFcmTokenRepository tokenRepository;
    private final FcmSender fcmSender;
    private final FcmSendHistoryRepository historyRepository;
    private final NoticeRepository noticeRepository;
    private final ManagerRepository managerRepository;
    private final SurveySetService surveySetService;
    private final FirebaseMessagingService firebaseMessagingService;

    /**
     * 조건에 맞는 사용자들에게 FCM 알림을 발송하고, 발송 이력을 기록한다.
     *
     * @param title    알림 제목
     * @param body     알림 본문
     * @param category 알림 카테고리 (DAILY, SPECIAL 등)
     * @param ageGroup 수신 대상 자녀의 연령대 (null이면 전체 대상)
     * @return 성공적으로 알림을 수신한 사용자 수
     */
    @Transactional
    public int sendNotificationToTarget(String title, String body, FcmCategory category, AgeGroup ageGroup) {
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

        for (UserFcmToken token : targets) {
            boolean sent = fcmSender.send(token.getFcmToken(), title, body);
            if (sent) {
                successCount++;
                noticeRepository.save(
                        Notice.builder()
                                .user(token.getUser())
                                .title(title)
                                .body(body)
                                .category(category != null ? category : FcmCategory.NOTICE)
                                .sentAt(now)
                                .build()
                );
            }
        }

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
    public int sendNotificationToUser(Users user, String title, String body, FcmCategory category, String url) {
        List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(user);
        int successCount = 0;
        LocalDateTime now = LocalDateTime.now();

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

    @Transactional
    public int sendNotificationToRiskGroupChildren() {
        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;
        int targetCount = 0;

        for (Users user : userRepository.findAll()) {
            if (user.getMember() == null) continue;

            for (Child child : user.getMember().getChildren()) {
                if (Boolean.TRUE.equals(child.getRiskGroup())) {
                    for (UserFcmToken token : tokenRepository.findByUserAndIsActiveTrue(user)) {
                        targetCount++;
                        boolean sent = fcmSender.send(
                                token.getFcmToken(),
                                child.getName() + "님이 위험군에 속해있어요!",
                                "지금 바로 특별 문진을 완료해주세요.."
                        );
                        if (sent) {
                            successCount++;
                            noticeRepository.save(
                                    Notice.builder()
                                            .user(user)
                                            .title(child.getName() + "님이 위험군에 속해있어요!")
                                            .body("지금 바로 특별 문진을 완료해주세요..")
                                            .category(FcmCategory.SPECIAL)
                                            .sentAt(now)
                                            .url("https://charti.site/specialSurvey?childId=" + child.getId())
                                            .build()
                            );
                        }
                    }
                }
            }
        }

        historyRepository.save(
                FcmSendHistory.builder()
                        .title("위험군 속한 자녀 대상")
                        .body("특별문진 요청 알림")
                        .category(FcmCategory.SPECIAL)
                        .targetCondition("riskGroup=true")
                        .targetCount(targetCount)
                        .successCount(successCount)
                        .sentAt(now)
                        .build()
        );

        return successCount;
    }

    /**
     * 그룹(targetGroup) 문진 알림.
     *
     * @param targetGroup Group.targetGroup 필드값 (e.g. "유치원")
     */
    @Transactional
    public int sendNotificationToGroupChildren(TargetGroup targetGroup) {
        LocalDateTime now = LocalDateTime.now();
        int successCount = 0, targetCount = 0;

        for (Users user : userRepository.findAll()) {
            if (user.getMember() == null) continue;

            for (Child child : user.getMember().getChildren()) {
                if (child.getGroup() != null && child.getGroup().getTargetGroup() == targetGroup) {
                    for (UserFcmToken token : tokenRepository.findByUserAndIsActiveTrue(user)) {
                        targetCount++;
                        boolean sent = fcmSender.send(
                                token.getFcmToken(),
                                child.getName() + "님, " +
                                        targetGroup.getDisplayName() +
                                        "에서 그룹 문진이 왔어요!",
                                "지금 바로 그룹 문진을 완료해주세요.."
                        );
                        if (sent) {
                            successCount++;
                            noticeRepository.save(
                                    Notice.builder()
                                            .user(user)
                                            .title(child.getName() +
                                                    "님, " +
                                                    targetGroup.getDisplayName() +
                                                    "에서 그룹 문진이 왔어요!")
                                            .body("지금 바로 그룹 문진을 완료해주세요..")
                                            .category(FcmCategory.SPECIAL)
                                            .sentAt(now)
                                            .url("https://charti.site/groupSurvey?childId=" + child.getId())
                                            .build()
                            );
                        }
                    }
                }
            }
        }

        historyRepository.save(
                FcmSendHistory.builder()
                        .title(targetGroup.getDisplayName() + " 그룹 대상자")
                        .body("그룹 문진 요청 알림")
                        .category(FcmCategory.SPECIAL)
                        .targetCondition("group=" + targetGroup.name())
                        .targetCount(targetCount)
                        .successCount(successCount)
                        .sentAt(now)
                        .build()
        );
        return successCount;
    }

    /**
     * [담당자 → 소속 그룹의 유저들에게 문진 세트 알림 전송]
     *
     * @param managerId 로그인한 담당자 ID
     * @param setId     발송할 문진 세트 ID
     */
    @Transactional
    public void sendSurveySetToGroupMembers(Long managerId, Long setId) {
        // 1. 문진 세트 및 관리자 정보 조회
        SurveySet set = surveySetService.getById(setId);
        String title = "[문진 요청] " + set.getSetTitle();
        String link = "https://charti.site/survey/set/" + setId;

        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저입니다."));
        Group group = manager.getGroup();

        // 2. 알림 대상자 조회 (해당 그룹 소속 유저 전체)
        List<Users> users = userRepository.findAllByManager_Group_Id(group.getId());

        // 3. 히스토리 엔티티 먼저 생성 (알림 1건 기준)
        FcmSendHistory history = historyRepository.save(
                FcmSendHistory.builder()
                        .sender(manager.getUsers()) // 알림 보낸 사람 = 관리자
                        .title(title)
                        .body(set.getSetTitle())
                        .category(FcmCategory.SPECIAL) // 또는 DAILY 등으로 상황에 맞게 지정
                        .link(link)
                        .sentAt(LocalDateTime.now())
                        .targetCount(users.size()) // 전체 대상자 수
                        .build()
        );

        // 4. 사용자별 FCM 전송 및 Notice 저장
        int successCount = 0;

        for (Users user : users) {
            List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(user);
            boolean sent = false;

            for (UserFcmToken token : tokens) {
                try {
                    firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, set.getSetTitle(), link);
                    sent = true;
                } catch (Exception e) {
                    log.warn("❌ FCM 발송 실패: userId={}, token={}", user.getId(), token.getFcmToken());
                }
            }

            if (sent) {
                successCount++;
            }

            // Notice는 항상 저장 (알림함 확인용)
            noticeRepository.save(
                    Notice.builder()
                            .user(user)
                            .title(title)
                            .body(set.getSetTitle())
                            .category(FcmCategory.SPECIAL)
                            .url(link)
                            .sentAt(LocalDateTime.now())
                            .build()
            );
        }

        // 5. 성공 개수 반영
        history.setSuccessCount(successCount);
    }

}
