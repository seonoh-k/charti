package com.example.demo.fcm.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.fcm.dto.AdminFcmSendResultDto;
import com.example.demo.fcm.dto.FcmSendResultDto;
import com.example.demo.fcm.dto.RecipientResultDto;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.entity.Notice;
import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.fcm.repository.FcmSendHistoryRepository;
import com.example.demo.fcm.repository.NoticeRepository;
import com.example.demo.fcm.repository.UserFcmTokenRepository;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import com.example.demo.users.entity.Admin;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    @Value("${app.domain}")
    private String appDomain;

    private final UserRepository userRepository;
    private final UserFcmTokenRepository tokenRepository;
    private final FcmSendHistoryRepository historyRepository;
    private final NoticeRepository noticeRepository;
    private final ManagerRepository managerRepository;
    private final SurveySetService surveySetService;
    private final FirebaseMessagingService firebaseMessagingService;
    private final ChildRepository childRepository;

    /**
     * 관리자 알림 발송 메인 서비스 메소드
     * - 컨트롤러로부터 모든 요청 파라미터를 받아 로직을 오케스트레이션합니다.
     */
    public AdminFcmSendResultDto sendAdvancedNotification(Admin sender, String targetType, AgeGroup ageGroup, TargetGroup targetGroup, FcmCategory fcmCategory, SurveyCategory surveyCategory, Long setId, String customTitle, String customBody) {

        Map<Users, List<Child>> targetUserMap = findTargetParentAndChildMap(targetType, ageGroup, targetGroup, surveyCategory);
        if (targetUserMap.isEmpty()) {
            return AdminFcmSendResultDto.createEmptyResult(targetType);
        }

        String targetCondition = String.format("target=%s, ageGroup=%s, targetGroup=%s, surveyCategory=%s, setId=%s",
                targetType, ageGroup, targetGroup, surveyCategory, (setId != null ? setId.toString() : "null"));

        return sendBulkNotificationAndSaveHistory(sender, targetUserMap, setId, customTitle, customBody, fcmCategory, targetCondition, targetType);
    }

    /**
     *  대상자 조회 로직 분리
     * - 각 조건에 맞는 사용자 목록을 DB에서 직접 조회
     */
    private List<Users> findTargetUsers(String targetType, AgeGroup ageGroup, TargetGroup targetGroup, SurveyCategory surveyCategory) { // 파라미터명을 category -> surveyCategory로 명확화

        // "SPECIAL_RISK" 대상: 위험군 사용자 조회
        if ("SPECIAL_RISK".equals(targetType)) {
            return userRepository.findUsersByChildRiskCategory(surveyCategory);
        }
        // "GROUP" 대상: 기관 그룹별 사용자 조회
        else if ("GROUP".equals(targetType) && targetGroup != null) {
            return userRepository.findUsersByChildrenInTargetGroup(targetGroup);
        }
        // "ALL" 대상: 전체 또는 연령대별 사용자 조회
        else if ("ALL".equals(targetType)) {
            // 1. 먼저 삭제되지 않은 모든 사용자를 DB에서 조회합니다.
            List<Users> allUsers = userRepository.findAllByDeletedFalse();

            // 2. 만약 ageGroup 필터가 있다면, 자바 코드로 2차 필터링을 수행합니다.
            if (ageGroup != null) {
                return allUsers.stream()
                        .filter(user -> user.getMember() != null &&
                                user.getMember().getChildren().stream()
                                        .anyMatch(child -> child.getAgeGroup() == ageGroup))
                        .collect(Collectors.toList());
            }
            return allUsers;
        }

        return new ArrayList<>();
    }
    /**
     *  대상자 조회 로직: 이제 학부모와 해당 자녀를 Map으로 묶어서 반환합니다.
     */
    private Map<Users, List<Child>> findTargetParentAndChildMap(String targetType, AgeGroup ageGroup, TargetGroup targetGroup, SurveyCategory surveyCategory) {
        List<Child> targetChildren = new ArrayList<>();

        if ("SPECIAL_RISK".equals(targetType)) {
            List<Child> childrenByRisk = (surveyCategory != null)
                    ? childRepository.findChildrenByRiskCategory(surveyCategory)
                    : childRepository.findByRiskGroupTrueAndParentUsersDeletedFalse();

            if (ageGroup != null) {
                targetChildren = childrenByRisk.stream().filter(c -> c.getAgeGroup() == ageGroup).collect(Collectors.toList());
            } else {
                targetChildren = childrenByRisk;
            }
        } else if ("GROUP".equals(targetType) && targetGroup != null) {
            List<Child> childrenInGroup = childRepository.findChildrenByTargetGroup(targetGroup);

            if (ageGroup != null) {
                targetChildren = childrenInGroup.stream().filter(c -> c.getAgeGroup() == ageGroup).collect(Collectors.toList());
            } else {
                targetChildren = childrenInGroup;
            }
        } else if ("ALL".equals(targetType)) {
            List<Users> allUsers = userRepository.findAllByDeletedFalse();
            if (ageGroup != null) {
                List<Users> filteredUsers = allUsers.stream()
                        .filter(user -> user.getMember() != null && user.getMember().getChildren().stream().anyMatch(child -> child.getAgeGroup() == ageGroup))
                        .collect(Collectors.toList());
                return filteredUsers.stream().collect(Collectors.toMap(user -> user, user -> new ArrayList<>()));
            } else {
                return allUsers.stream().collect(Collectors.toMap(user -> user, user -> new ArrayList<>()));
            }
        }

        if (targetChildren.isEmpty()) return new HashMap<>();

        return targetChildren.stream()
                .filter(c -> c.getParent() != null && c.getParent().getUsers() != null && !c.getParent().getUsers().isDeleted())
                .collect(Collectors.groupingBy(child -> child.getParent().getUsers()));
    }


    /**
     *  발송 및 저장 로직 통합
     */
    private AdminFcmSendResultDto sendBulkNotificationAndSaveHistory(Admin sender, Map<Users, List<Child>> targetUserMap, Long setId, String customTitle, String customBody, FcmCategory fcmCategory, String targetCondition, String targetType) {
        List<RecipientResultDto> successfulRecipients = new ArrayList<>();
        List<RecipientResultDto> failedRecipients = new ArrayList<>();
        List<Notice> noticesToSave = new ArrayList<>();

        SurveySet set = (setId != null) ? surveySetService.getById(setId) : null;

        for (Map.Entry<Users, List<Child>> entry : targetUserMap.entrySet()) {
            Users user = entry.getKey();
            List<Child> children = entry.getValue();

            String personalizedTitle, personalizedBody, personalizedUrl = null;

            if (set != null && !children.isEmpty()) {
                String childrenNames = children.stream().map(Child::getName).collect(Collectors.joining(", "));
                personalizedTitle = String.format("[문진 요청] %s 어린이를 위한 새 문진이 도착했어요!", childrenNames);
                personalizedBody = String.format("'%s' 문진을 확인하고 답변을 부탁드립니다.", set.getSetTitle());
                personalizedUrl = appDomain + "/surveySet/request/" + setId + "?childId=" + children.get(0).getId();
            } else {
                personalizedTitle = customTitle;
                personalizedBody = customBody;
            }

            noticesToSave.add(Notice.builder().user(user).title(personalizedTitle).body(personalizedBody).category(fcmCategory).url(personalizedUrl).sentAt(LocalDateTime.now()).build());
            boolean sent = sendFcmOnly(user, personalizedTitle, personalizedBody, personalizedUrl);
            if (sent) {
                List<String> childNames = children.stream().map(Child::getName).collect(Collectors.toList());
                successfulRecipients.add(new RecipientResultDto(user.getName(), childNames));
            } else {
                List<String> childNames = children.stream().map(Child::getName).collect(Collectors.toList());
                failedRecipients.add(new RecipientResultDto(user.getName(), childNames));
            }
        }

        noticeRepository.saveAll(noticesToSave);

        String historyTitle;
        String historyBody = String.format("관리자(%s)가 발송한 알림", sender.getName());
        String historyLink = null;

        if (set != null) {
            historyTitle = set.getSetTitle();
            historyLink = appDomain + "/admin/surveySet/" + setId;
        } else {
            historyTitle = customTitle;
        }

        historyRepository.save(FcmSendHistory.builder()
                .adminSender(sender)
                .title(historyTitle)
                .body(historyBody)
                .category(fcmCategory)
                .targetCondition(targetCondition)
                .targetCount(targetUserMap.size())
                .successCount(successfulRecipients.size())
                .sentAt(LocalDateTime.now())
                .link(historyLink)
                .build());

        return AdminFcmSendResultDto.builder()
                .targetType(targetType)
                .totalTargetCount(targetUserMap.size())
                .successCount(successfulRecipients.size())
                .successfulRecipients(successfulRecipients)
                .failedRecipients(failedRecipients)
                .build();
    }

    /**
     *  FCM 발송만 담당하는 private 헬퍼 메소드
     */
    private boolean sendFcmOnly(Users user, String title, String body, String url) {
        List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(user);
        if (tokens.isEmpty()) return false;
        boolean sentSuccessfully = false;
        for (UserFcmToken token : tokens) {
            try {
                firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, body, url);
                sentSuccessfully = true;
            } catch (Exception e) { log.warn("FCM 발송 실패: userId={}, token={}", user.getId(), token.getFcmToken(), e); }
        }
        return sentSuccessfully;
    }

    /**
     * 특정 사용자 한 명에게 알림을 보내는 핵심 메소드.
     */
    public int sendNotification(Admin sender, Users recipient, String title, String body, FcmCategory category, String url) {
        if (recipient == null || recipient.isDeleted()) {
            log.warn("유효하지 않은 수신자에게 알림을 보낼 수 없습니다. recipientId: {}", recipient != null ? recipient.getId() : "null");
            return 0;
        }

        // 1. Notice를 먼저 저장합니다.
        noticeRepository.save(
                Notice.builder()
                        .user(recipient).title(title).body(body)
                        .category(category).url(url).sentAt(LocalDateTime.now())
                        .build()
        );

        // 2. FCM 발송을 시도합니다.
        boolean sentSuccessfully = false;
        try {
            List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(recipient);
            if (!tokens.isEmpty()) {
                for (UserFcmToken token : tokens) {
                    firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, body, url);
                    sentSuccessfully = true; // 한 번이라도 예외 없이 호출되면 성공으로 간주
                }
            } else {
                log.info("수신자에게 등록된 활성 토큰이 없어 FCM을 보내지 않습니다. recipientId: {}", recipient.getId());
            }
        } catch (Exception e) {
            log.error("FCM 발송 처리 중 오류 발생", e);
        }

        // 3. 발송 이력을 저장합니다.
        try {
            historyRepository.save(
                    FcmSendHistory.builder()
                            .adminSender(sender).title(title).body(body).category(category)
                            .targetCondition("userId=" + recipient.getId()).targetCount(1)
                            .successCount(sentSuccessfully ? 1 : 0).sentAt(LocalDateTime.now()).link(url)
                            .build()
            );
        } catch (Exception e) {
            log.error("FCM 발송 이력 저장 실패", e);
        }

        return sentSuccessfully ? 1 : 0;
    }


    /**
     * 매칭 완료 알림 등 다른 시스템에서 사용
     */
    public int sendMatchingNotification(Users sender, Users recipient, String title, String body, FcmCategory category, String url) {
        if (recipient == null || recipient.isDeleted()) {
            log.warn("유효하지 않은 수신자에게 알림을 보낼 수 없습니다. recipientId: {}", recipient != null ? recipient.getId() : "null");
            return 0;
        }

        // 1. Notice를 먼저 저장합니다.
        noticeRepository.save(
                Notice.builder()
                        .user(recipient).title(title).body(body)
                        .category(category).url(url).sentAt(LocalDateTime.now())
                        .build()
        );

        // 2. FCM 발송을 시도합니다.
        boolean sentSuccessfully = false;
        try {
            List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(recipient);
            if (!tokens.isEmpty()) {
                for (UserFcmToken token : tokens) {
                    firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, body, url);
                    sentSuccessfully = true; // 한 번이라도 예외 없이 호출되면 성공으로 간주
                }
            } else {
                log.info("수신자에게 등록된 활성 토큰이 없어 FCM을 보내지 않습니다. recipientId: {}", recipient.getId());
            }
        } catch (Exception e) {
            log.error("FCM 발송 처리 중 오류 발생", e);
        }

        // 3. 발송 이력을 저장합니다.
        try {
            historyRepository.save(
                    FcmSendHistory.builder()
                            .sender(sender).title(title).body(body).category(category)
                            .targetCondition("userId=" + recipient.getId()).targetCount(1)
                            .successCount(sentSuccessfully ? 1 : 0).sentAt(LocalDateTime.now()).link(url)
                            .build()
            );
        } catch (Exception e) {
            log.error("FCM 발송 이력 저장 실패", e);
        }

        return sentSuccessfully ? 1 : 0;
    }

    /**
     * 담당자용 문진 세트 발송
     */
    public FcmSendResultDto sendSurveySetToGroupMembers(Long managerId, Long setId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저입니다. ID: " + managerId));
        SurveySet set = surveySetService.getById(setId);
        List<Child> targetChildren = childRepository.findByGroupId(manager.getGroup().getId());

        if (targetChildren.isEmpty()) {
            log.info("그룹 ID {} 에 해당하는 자녀가 없어 알림을 발송하지 않습니다.", manager.getGroup().getId());
            return new FcmSendResultDto(0, 0, new ArrayList<>(), new ArrayList<>());
        }

        List<String> successfulNames = new ArrayList<>();
        List<String> failedNames = new ArrayList<>();
        List<Notice> noticesToSave = new ArrayList<>();
        Users sender = manager.getUsers();

        TargetGroup targetGroup = manager.getGroup().getTargetGroup();
        String targetCondition = String.format("target=GROUP, targetGroup=%s, setId=%s",
                targetGroup.name(),
                setId.toString());

        for (Child child : targetChildren) {
            Users parent = child.getParent().getUsers();
            if (parent == null || parent.isDeleted()) {
                failedNames.add(child.getName() + " (학부모 정보 없음)");
                continue;
            }

            String title = String.format("[문진 요청] %s 어린이를 위한 새 문진이 도착했어요!", child.getName());
            String body = String.format("'%s' 문진을 확인하고 답변을 부탁드립니다.", set.getSetTitle());
            String link = "http://localhost:8080/surveySet/request/" + setId + "?childId=" + child.getId();

            noticesToSave.add(Notice.builder().user(parent).title(title).body(body)
                    .category(FcmCategory.GROUP).url(link).sentAt(LocalDateTime.now()).build());

            boolean sent = sendFcmOnly(parent, title, body, link);

            if (sent) {
                successfulNames.add(child.getName());
            } else {
                failedNames.add(child.getName() + " (발송 실패)");
            }
        }

        noticeRepository.saveAll(noticesToSave);

        historyRepository.save(
                FcmSendHistory.builder()
                        .sender(sender)
                        .title(set.getSetTitle())
                        .body(String.format("%s 담당자가 그룹 문진을 발송했습니다.", sender.getName()))
                        .category(FcmCategory.GROUP)
                        .targetCondition(targetCondition)
                        .link("/surveySet/request/" + setId)
                        .targetCount(targetChildren.size())
                        .successCount(successfulNames.size())
                        .sentAt(LocalDateTime.now())
                        .build()
        );

        return new FcmSendResultDto(targetChildren.size(), successfulNames.size(), successfulNames, failedNames);
    }

}