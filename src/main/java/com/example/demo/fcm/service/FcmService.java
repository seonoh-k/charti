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
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Manager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ChildRepository childRepository;

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
     * 담당자가 소속 그룹의 학부모들에게 '자녀별'로 '문진 세트' 알림을 발송합니다.
     *
     * @param managerId 알림을 보내는 담당자의 ID
     * @param setId     발송할 문진 세트의 ID
     */
    public void sendSurveySetToGroupMembers(Long managerId, Long setId) {
        // 1. 필요한 정보 조회
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저입니다. ID: " + managerId));
        SurveySet set = surveySetService.getById(setId);

        // 2. 알림 대상 '자녀' 목록 조회
        List<Child> targetChildren = childRepository.findByGroupId(manager.getGroup().getId());
        if (targetChildren.isEmpty()) {
            log.info("그룹 ID {} 에 해당하는 자녀가 없어 알림을 발송하지 않습니다.", manager.getGroup().getId());
            return; // 대상자가 없으면 종료
        }

        LocalDateTime sentTime = LocalDateTime.now();
        int successCount = 0;
        List<Notice> noticesToSave = new ArrayList<>();
        String commonLink = "https://charti.site/surveySet/request/" + setId;
        // 3. 자녀 한 명 한 명을 기준으로 반복
        for (Child child : targetChildren) {
            // Member를 통해 최종 Users 객체를 가져옵니다.
            // child.getParent() -> Member, child.getParent().getUser() -> Users
            Users parent = child.getParent().getUsers();
            if (parent == null || parent.isDeleted()) continue;
            // 4. 자녀별로 개별 메시지 생성
            String title = String.format("[문진 요청] %s 어린이를 위한 새 문진이 도착했어요!", child.getName());
            String body = String.format("'%s' 문진을 확인하고 답변을 부탁드립니다.", set.getSetTitle());
            String link = "https://charti.site/surveySet/request/" + setId + "?childId=" + child.getId();
            // 5. 학부모의 활성 토큰으로 FCM 발송
            List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(parent);
            if (tokens.isEmpty()) continue;

            boolean sentToUser = false;
            for (UserFcmToken token : tokens) {
                try {
                    firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, body, link);
                    sentToUser = true;
                } catch (Exception e) {
                    log.warn("❌ FCM 발송 실패: userId={}, token={}", parent.getId(), token.getFcmToken(), e);
                }
            }
            if (sentToUser) successCount++;
            // 6. 생성된 개별 Notice를 리스트에 추가
            noticesToSave.add(
                    Notice.builder()
                            .user(parent)
                            .title(title)
                            .body(body)
                            .category(FcmCategory.GROUP)
                            .url(link)
                            .sentAt(sentTime)
                            .build()
            );
        }
        // 7. 모든 Notice를 DB에 일괄 저장
        noticeRepository.saveAll(noticesToSave);
        // 8. 최종 결과로 발송 이력(History) 저장
        historyRepository.save(
                FcmSendHistory.builder()
                        .sender(manager.getUsers())
                        .title(set.getSetTitle())
                        .body(String.format("%s 담당자가 그룹 문진을 발송했습니다.", manager.getUsers().getName()))
                        .category(FcmCategory.GROUP)
                        .targetCondition("groupId=" + manager.getGroup().getId())
                        .link(commonLink)
                        .targetCount(targetChildren.size())
                        .successCount(successCount)
                        .sentAt(sentTime)
                        .build()
        );
    }
}
