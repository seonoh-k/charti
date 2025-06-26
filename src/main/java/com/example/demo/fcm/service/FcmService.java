package com.example.demo.fcm.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.fcm.dto.AdminFcmSendResultDto;
import com.example.demo.fcm.dto.FcmSendResultDto;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.entity.Notice;
import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.fcm.repository.FcmSendHistoryRepository;
import com.example.demo.fcm.repository.NoticeRepository;
import com.example.demo.fcm.repository.UserFcmTokenRepository;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
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
     * [역할 명확화] 특정 사용자 한 명에게 알림을 보내고, Notice와 History를 모두 저장하는 핵심 메소드.
     * 스케줄러, 상담 배정 등 개별 알림에서만 사용합니다.
     */
    public int sendNotification(Users sender, Users recipient, String title, String body, FcmCategory category, String url) {
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
     * [수정] '전체' 또는 '연령/카테고리' 대상 발송 메소드
     */
    public AdminFcmSendResultDto sendNotificationToTarget(Users sender, String title, String body, FcmCategory category, AgeGroup ageGroup) {
        // TODO: 이 메소드는 현재 성능 이슈(findAll)가 있습니다. 추후 개선이 필요합니다.
        List<Users> targetUsers = userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .filter(user -> ageGroup == null || (
                        user.getMember() != null &&
                                user.getMember().getChildren().stream()
                                        .anyMatch(child -> child.getAgeGroup() == ageGroup)
                ))
                .collect(Collectors.toList());

        int successCount = 0;
        List<Notice> noticesToSave = new ArrayList<>();

        for (Users user : targetUsers) {
            // Notice 객체를 먼저 생성하여 리스트에 추가합니다.
            noticesToSave.add(Notice.builder().user(user).title(title).body(body)
                    .category(category).sentAt(LocalDateTime.now()).build());

            // FCM 발송을 시도합니다.
            boolean sent = sendFcmOnly(user, title, body, null);
            if(sent) successCount++;
        }

        // 생성된 모든 Notice를 한 번에 저장합니다.
        noticeRepository.saveAll(noticesToSave);

        // History를 단 1건만 저장합니다.
        historyRepository.save(FcmSendHistory.builder()
                .sender(sender)
                .title(title).body(body).category(category)
                .targetCondition("ageGroup=" + (ageGroup != null ? ageGroup.name() : "ALL"))
                .targetCount(targetUsers.size()).successCount(successCount).sentAt(LocalDateTime.now())
                .build());

        return AdminFcmSendResultDto.builder()
                .targetType(ageGroup == null ? "ALL" : "FILTER")
                .totalTargetCount(targetUsers.size())
                .successCount(successCount)
                .build();
    }

    /**
     * [수정] '위험군' 대상 발송 메소드
     */
    public AdminFcmSendResultDto sendNotificationToRiskGroupChildren(Users sender) {
        // TODO: 이 메소드는 현재 성능 이슈(findAll)가 있습니다. 추후 개선이 필요합니다.
        List<Users> allUsers = userRepository.findAll();

        List<String> successfulNames = new ArrayList<>();
        List<String> failedNames = new ArrayList<>();
        List<Users> targetUsers = new ArrayList<>();
        List<Notice> noticesToSave = new ArrayList<>();

        for(Users user : allUsers) {
            if (user.getMember() != null &&
                    user.getMember().getChildren().stream().anyMatch(c -> Boolean.TRUE.equals(c.getRiskGroup()))) {
                targetUsers.add(user);
            }
        }

        String title = "우리아이 돌봄 특별 문진 요청";
        String link = "https://localhost:8080/specialSurvey";

        for(Users user : targetUsers) {
            String childrenNames = user.getMember().getChildren().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getRiskGroup()))
                    .map(Child::getName)
                    .collect(Collectors.joining(", "));
            String body = String.format("%s님, %s 어린이의 건강 상태 확인을 위한 특별 문진을 작성해주세요.", user.getName(), childrenNames);

            noticesToSave.add(Notice.builder().user(user).title(title).body(body).category(FcmCategory.SPECIAL).url(link).sentAt(LocalDateTime.now()).build());

            boolean sent = sendFcmOnly(user, title, body, link);
            if(sent) {
                successfulNames.add(user.getName());
            } else {
                failedNames.add(user.getName() + " (발송 실패)");
            }
        }

        noticeRepository.saveAll(noticesToSave);

        historyRepository.save(FcmSendHistory.builder()
                .sender(sender).title(title).body("위험군 자녀 대상 특별문진 요청")
                .category(FcmCategory.SPECIAL).targetCondition("riskGroup=true")
                .targetCount(targetUsers.size()).successCount(successfulNames.size())
                .sentAt(LocalDateTime.now()).link(link).build());

        return AdminFcmSendResultDto.builder()
                .targetType("RISK").totalTargetCount(targetUsers.size())
                .successCount(successfulNames.size()).successfulRecipientNames(successfulNames)
                .failedRecipientNames(failedNames).build();
    }

    /**
     * [수정] '특정 기관 그룹' 대상 발송 메소드
     */
    public AdminFcmSendResultDto sendNotificationToGroupChildren(Users sender, TargetGroup targetGroup) {
        // TODO: userRepository.findParentsWithChildrenInGroup(groupId) 같은 최적화된 쿼리 필요
        List<Users> allUsers = userRepository.findAll();

        List<String> successfulNames = new ArrayList<>();
        List<String> failedNames = new ArrayList<>();
        List<Users> targetUsers = new ArrayList<>();
        List<Notice> noticesToSave = new ArrayList<>();

        for (Users user : allUsers) {
            if (user.getMember() != null &&
                    user.getMember().getChildren().stream()
                            .anyMatch(c -> c.getGroup() != null && c.getGroup().getTargetGroup() == targetGroup)) {
                targetUsers.add(user);
            }
        }

        String title = targetGroup.getDisplayName() + " 그룹 문진 요청";

        for(Users user : targetUsers) {
            String body = user.getName() + "님, 소속 그룹의 문진을 확인해주세요.";
            noticesToSave.add(Notice.builder().user(user).title(title).body(body).category(FcmCategory.GROUP).sentAt(LocalDateTime.now()).build());
            boolean sent = sendFcmOnly(user, title, body, null);

            if(sent) {
                successfulNames.add(user.getName());
            } else {
                failedNames.add(user.getName() + " (발송 실패)");
            }
        }

        noticeRepository.saveAll(noticesToSave);

        historyRepository.save(FcmSendHistory.builder()
                .sender(sender)
                .title(title).body("그룹 문진 요청 알림")
                .category(FcmCategory.GROUP).targetCondition("group=" + targetGroup.name())
                .targetCount(targetUsers.size()).successCount(successfulNames.size())
                .sentAt(LocalDateTime.now()).build());

        return AdminFcmSendResultDto.builder()
                .targetType("GROUP")
                .totalTargetCount(targetUsers.size())
                .successCount(successfulNames.size())
                .successfulRecipientNames(successfulNames)
                .failedRecipientNames(failedNames)
                .build();
    }

    /**
     * [수정] 담당자용 문진 세트 발송
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

        for (Child child : targetChildren) {
            Users parent = child.getParent().getUsers();
            if (parent == null || parent.isDeleted()) {
                failedNames.add(child.getName() + " (학부모 정보 없음)");
                continue;
            }

            String title = String.format("[문진 요청] %s 어린이를 위한 새 문진이 도착했어요!", child.getName());
            String body = String.format("'%s' 문진을 확인하고 답변을 부탁드립니다.", set.getSetTitle());
            String link = "https://localhost:8080/surveySet/request/" + setId + "?childId=" + child.getId();

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
                        .targetCondition("groupId=" + manager.getGroup().getId())
                        .link("https://localhost:8080/surveySet/request/" + setId)
                        .targetCount(targetChildren.size())
                        .successCount(successfulNames.size())
                        .sentAt(LocalDateTime.now())
                        .build()
        );

        return new FcmSendResultDto(targetChildren.size(), successfulNames.size(), successfulNames, failedNames);
    }

    /**
     * [추가] FCM 발송만 담당하는 private 헬퍼 메소드
     */
    private boolean sendFcmOnly(Users user, String title, String body, String url) {
        List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(user);
        if (tokens.isEmpty()) return false;

        boolean sentSuccessfully = false;
        for (UserFcmToken token : tokens) {
            try {
                firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, body, url);
                sentSuccessfully = true;
            } catch (Exception e) {
                log.warn("FCM 발송 실패: userId={}, token={}", user.getId(), token.getFcmToken(), e);
            }
        }
        return sentSuccessfully;
    }
}
