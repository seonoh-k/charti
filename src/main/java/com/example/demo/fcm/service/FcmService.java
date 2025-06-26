//package com.example.demo.fcm.service;
//
//import com.example.demo.entity.Group;
//import com.example.demo.enums.TargetGroup;
//import com.example.demo.enums.AgeGroup;
//import com.example.demo.enums.FcmCategory;
//import com.example.demo.fcm.dto.FcmSendResultDto;
//import com.example.demo.fcm.entity.FcmSendHistory;
//import com.example.demo.fcm.entity.Notice;
//import com.example.demo.fcm.entity.UserFcmToken;
//import com.example.demo.fcm.repository.FcmSendHistoryRepository;
//import com.example.demo.fcm.repository.NoticeRepository;
//import com.example.demo.fcm.repository.UserFcmTokenRepository;
//import com.example.demo.survey.entity.SurveySet;
//import com.example.demo.survey.service.SurveySetService;
//import com.example.demo.users.entity.Users;
//import com.example.demo.users.repository.ChildRepository;
//import com.example.demo.users.repository.ManagerRepository;
//import com.example.demo.users.repository.UserRepository;
//import com.example.demo.users.entity.Child;
//import com.example.demo.users.entity.Manager;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class FcmService {
//
//    private final UserRepository userRepository;
//    private final UserFcmTokenRepository tokenRepository;
//    private final FcmSender fcmSender;
//    private final FcmSendHistoryRepository historyRepository;
//    private final NoticeRepository noticeRepository;
//    private final ManagerRepository managerRepository;
//    private final SurveySetService surveySetService;
//    private final FirebaseMessagingService firebaseMessagingService;
//    private final ChildRepository childRepository;
//
//    /**
//     * 조건에 맞는 사용자들에게 FCM 알림을 발송하고, 발송 이력을 기록한다.
//     *
//     * @param title    알림 제목
//     * @param body     알림 본문
//     * @param category 알림 카테고리 (DAILY, SPECIAL 등)
//     * @param ageGroup 수신 대상 자녀의 연령대 (null이면 전체 대상)
//     * @return 성공적으로 알림을 수신한 사용자 수
//     */
//    @Transactional
//    public int sendNotificationToTarget(String title, String body, FcmCategory category, AgeGroup ageGroup) {
//        List<UserFcmToken> targets = tokenRepository.findAll().stream()
//                .filter(token -> !token.getUser().isDeleted())
//                .filter(token -> ageGroup == null || (
//                        token.getUser().getMember() != null &&
//                                token.getUser().getMember().getChildren().stream()
//                                        .anyMatch(child -> child.getAgeGroup() == ageGroup)
//                ))
//                .toList();
//
//        int successCount = 0;
//        LocalDateTime now = LocalDateTime.now();
//
//        for (UserFcmToken token : targets) {
//            boolean sent = fcmSender.send(token.getFcmToken(), title, body);
//            if (sent) {
//                successCount++;
//                noticeRepository.save(
//                        Notice.builder()
//                                .user(token.getUser())
//                                .title(title)
//                                .body(body)
//                                .category(category != null ? category : FcmCategory.NOTICE)
//                                .sentAt(now)
//                                .build()
//                );
//            }
//        }
//
//        historyRepository.save(
//                FcmSendHistory.builder()
//                        .title(title)
//                        .body(body)
//                        .category(category != null ? category : FcmCategory.NOTICE)
//                        .targetCondition("ageGroup=" + (ageGroup != null ? ageGroup.name() : "ALL"))
//                        .targetCount(targets.size())
//                        .successCount(successCount)
//                        .sentAt(now)
//                        .build()
//        );
//
//        return successCount;
//    }
//
//    /**
//     * 특정 사용자에게만 FCM + Notice + SendHistory 저장
//     */
//    @Transactional
//    public int sendNotificationToUser(Users user, String title, String body, FcmCategory category, String url) {
//        List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(user);
//        int successCount = 0;
//        LocalDateTime now = LocalDateTime.now();
//
//        for (UserFcmToken token : tokens) {
//            boolean sent = fcmSender.send(token.getFcmToken(), title, body);
//            if (sent) {
//                successCount++;
//                noticeRepository.save(
//                        Notice.builder()
//                                .user(user)
//                                .title(title)
//                                .body(body)
//                                .category(category != null ? category : FcmCategory.DAILY)
//                                .sentAt(now)
//                                .url(url)
//                                .build()
//                );
//            }
//        }
//
//        historyRepository.save(
//                FcmSendHistory.builder()
//                        .title(title)
//                        .body(body)
//                        .category(category)
//                        .targetCondition("userId=" + user.getId())
//                        .targetCount(tokens.size())
//                        .successCount(successCount)
//                        .sentAt(now)
//                        .build()
//        );
//
//        return successCount;
//    }
//
//    @Transactional
//    public int sendNotificationToRiskGroupChildren() {
//        LocalDateTime now = LocalDateTime.now();
//        int successCount = 0;
//        int targetCount = 0;
//
//        for (Users user : userRepository.findAll()) {
//            if (user.getMember() == null) continue;
//
//            for (Child child : user.getMember().getChildren()) {
//                if (Boolean.TRUE.equals(child.getRiskGroup())) {
//                    for (UserFcmToken token : tokenRepository.findByUserAndIsActiveTrue(user)) {
//                        targetCount++;
//                        boolean sent = fcmSender.send(
//                                token.getFcmToken(),
//                                child.getName() + "님이 위험군에 속해있어요!",
//                                "지금 바로 특별 문진을 완료해주세요.."
//                        );
//                        if (sent) {
//                            successCount++;
//                            noticeRepository.save(
//                                    Notice.builder()
//                                            .user(user)
//                                            .title(child.getName() + "님이 위험군에 속해있어요!")
//                                            .body("지금 바로 특별 문진을 완료해주세요..")
//                                            .category(FcmCategory.SPECIAL)
//                                            .sentAt(now)
//                                            .url("https://charti.site/specialSurvey?childId=" + child.getId())
//                                            .build()
//                            );
//                        }
//                    }
//                }
//            }
//        }
//
//        historyRepository.save(
//                FcmSendHistory.builder()
//                        .title("위험군 속한 자녀 대상")
//                        .body("특별문진 요청 알림")
//                        .category(FcmCategory.SPECIAL)
//                        .targetCondition("riskGroup=true")
//                        .targetCount(targetCount)
//                        .successCount(successCount)
//                        .sentAt(now)
//                        .build()
//        );
//
//        return successCount;
//    }
//
//    /**
//     * 그룹(targetGroup) 문진 알림.
//     *
//     * @param targetGroup Group.targetGroup 필드값 (e.g. "유치원")
//     */
//    @Transactional
//    public int sendNotificationToGroupChildren(TargetGroup targetGroup) {
//        LocalDateTime now = LocalDateTime.now();
//        int successCount = 0, targetCount = 0;
//
//        for (Users user : userRepository.findAll()) {
//            if (user.getMember() == null) continue;
//
//            for (Child child : user.getMember().getChildren()) {
//                if (child.getGroup() != null && child.getGroup().getTargetGroup() == targetGroup) {
//                    for (UserFcmToken token : tokenRepository.findByUserAndIsActiveTrue(user)) {
//                        targetCount++;
//                        boolean sent = fcmSender.send(
//                                token.getFcmToken(),
//                                child.getName() + "님, " +
//                                        targetGroup.getDisplayName() +
//                                        "에서 그룹 문진이 왔어요!",
//                                "지금 바로 그룹 문진을 완료해주세요.."
//                        );
//                        if (sent) {
//                            successCount++;
//                            noticeRepository.save(
//                                    Notice.builder()
//                                            .user(user)
//                                            .title(child.getName() +
//                                                    "님, " +
//                                                    targetGroup.getDisplayName() +
//                                                    "에서 그룹 문진이 왔어요!")
//                                            .body("지금 바로 그룹 문진을 완료해주세요..")
//                                            .category(FcmCategory.SPECIAL)
//                                            .sentAt(now)
//                                            .url("https://charti.site/groupSurvey?childId=" + child.getId())
//                                            .build()
//                            );
//                        }
//                    }
//                }
//            }
//        }
//
//        historyRepository.save(
//                FcmSendHistory.builder()
//                        .title(targetGroup.getDisplayName() + " 그룹 대상자")
//                        .body("그룹 문진 요청 알림")
//                        .category(FcmCategory.SPECIAL)
//                        .targetCondition("group=" + targetGroup.name())
//                        .targetCount(targetCount)
//                        .successCount(successCount)
//                        .sentAt(now)
//                        .build()
//        );
//        return successCount;
//    }
//
//    /**
//     * 담당자가 소속 그룹의 학부모들에게 '자녀별'로 '문진 세트' 알림을 발송합니다.
//     *
//     * @param managerId 알림을 보내는 담당자의 ID
//     * @param setId     발송할 문진 세트의 ID
//     */
//    public FcmSendResultDto sendSurveySetToGroupMembers(Long managerId, Long setId) {
//        // 1. 필요한 정보 조회
//        Manager manager = managerRepository.findById(managerId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저입니다. ID: " + managerId));
//        SurveySet set = surveySetService.getById(setId);
//        List<Child> targetChildren = childRepository.findByGroupId(manager.getGroup().getId());
//
//        if (targetChildren.isEmpty()) {
//            log.info("그룹 ID {} 에 해당하는 자녀가 없어 알림을 발송하지 않습니다.", manager.getGroup().getId());
//            return new FcmSendResultDto(0, 0, new ArrayList<>(), new ArrayList<>());
//        }
//
//        LocalDateTime sentTime = LocalDateTime.now();
//        List<Notice> noticesToSave = new ArrayList<>();
//        List<String> successfulNames = new ArrayList<>();
//        List<String> failedNames = new ArrayList<>();
//
//        // 2. 자녀 한 명 한 명을 기준으로 반복하며 성공/실패 명단 기록
//        for (Child child : targetChildren) {
//            Users parent = child.getParent().getUsers();
//            if (parent == null || parent.isDeleted()) {
//                failedNames.add(child.getName() + " (학부모 정보 없음)");
//                continue;
//            }
//
//            String title = String.format("[문진 요청] %s 어린이를 위한 새 문진이 도착했어요!", child.getName());
//            String body = String.format("'%s' 문진을 확인하고 답변을 부탁드립니다.", set.getSetTitle());
//            String link = "https://localhost:8080/surveySet/request/" + setId + "?childId=" + child.getId();
//
//            List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(parent);
//            if (tokens.isEmpty()) {
//                failedNames.add(child.getName() + " (등록된 알림 토큰 없음)");
//                continue;
//            }
//
//            boolean sentToUser = false;
//            for (UserFcmToken token : tokens) {
//                try {
//                    firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, body, link);
//                    sentToUser = true;
//                } catch (Exception e) {
//                    log.warn("❌ FCM 발송 실패: userId={}, token={}", parent.getId(), token.getFcmToken(), e);
//                }
//            }
//
//            if (sentToUser) {
//                successfulNames.add(child.getName());
//            } else {
//                failedNames.add(child.getName() + " (FCM 전송 실패)");
//            }
//
//            noticesToSave.add(
//                    Notice.builder()
//                            .user(parent)
//                            .title(title)
//                            .body(body)
//                            .category(FcmCategory.GROUP)
//                            .url(link)
//                            .sentAt(sentTime)
//                            .build()
//            );
//        }
//
//        // 3. DB 작업 및 이력 저장
//        noticeRepository.saveAll(noticesToSave);
//        historyRepository.save(
//                FcmSendHistory.builder()
//                        .sender(manager.getUsers())
//                        .title(set.getSetTitle())
//                        .body(String.format("%s 담당자가 그룹 문진을 발송했습니다.", manager.getUsers().getName()))
//                        .category(FcmCategory.GROUP)
//                        .targetCondition("groupId=" + manager.getGroup().getId())
//                        .link("https://charti.site/surveySet/request/" + setId)
//                        .targetCount(targetChildren.size())
//                        .successCount(successfulNames.size()) // 성공한 이름의 개수로 카운트
//                        .sentAt(sentTime)
//                        .build()
//        );
//
//        // 4. 최종 결과를 DTO에 담아 반환
//        return new FcmSendResultDto(targetChildren.size(), successfulNames.size(), successfulNames, failedNames);
//    }
//}

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

    public int sendNotification(Users sender, Users recipient, String title, String body, FcmCategory category, String url) {
        if (recipient == null || recipient.isDeleted()) {
            log.warn("유효하지 않은 수신자에게 알림을 보낼 수 없습니다. recipientId: {}", recipient != null ? recipient.getId() : "null");
            return 0;
        }

        List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(recipient);
        if (tokens.isEmpty()) {
            log.info("수신자에게 등록된 활성 토큰이 없어 알림을 보내지 않습니다. recipientId: {}", recipient.getId());
            return 0;
        }

        boolean sentSuccessfully = false;
        for (UserFcmToken token : tokens) {
            try {
                firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, body, url);
                sentSuccessfully = true; // 한 번이라도 성공하면 true
            } catch (Exception e) {
                log.warn("FCM 발송 실패: recipientId={}, token={}", recipient.getId(), token.getFcmToken(), e);
            }
        }

        if (sentSuccessfully) {
            noticeRepository.save(
                    Notice.builder()
                            .user(recipient)
                            .title(title)
                            .body(body)
                            .category(category)
                            .url(url)
                            .sentAt(LocalDateTime.now())
                            .build()
            );

            historyRepository.save(
                    FcmSendHistory.builder()
                            .sender(sender) // [수정] 파라미터로 받은 sender를 저장
                            .title(title)
                            .body(body)
                            .category(category)
                            .targetCondition("userId=" + recipient.getId())
                            .targetCount(1)
                            .successCount(1)
                            .sentAt(LocalDateTime.now())
                            .link(url)
                            .build()
            );
            return 1;
        }

        return 0;
    }

    /**
     * [수정] '전체' 또는 '연령/카테고리' 대상 발송 메소드
     */
    public AdminFcmSendResultDto sendNotificationToTarget(String title, String body, FcmCategory category, AgeGroup ageGroup) {
        // TODO: 이 메소드는 현재 성능 이슈(findAll)가 있습니다. 추후 개선이 필요합니다.
        List<UserFcmToken> targets = tokenRepository.findAll().stream()
                .filter(token -> !token.getUser().isDeleted())
                .filter(token -> ageGroup == null || (
                        token.getUser().getMember() != null &&
                                token.getUser().getMember().getChildren().stream()
                                        .anyMatch(child -> child.getAgeGroup() == ageGroup)
                ))
                .collect(Collectors.toList());

        int successCount = 0;
        // Notice 저장은 생략 (전체 발송은 양이 너무 많음)
        for (UserFcmToken token : targets) {
            boolean sent = fcmSender.send(token.getFcmToken(), title, body);
            if (sent) successCount++;
        }

        historyRepository.save(FcmSendHistory.builder()
                .title(title).body(body).category(category)
                .targetCondition("ageGroup=" + (ageGroup != null ? ageGroup.name() : "ALL"))
                .targetCount(targets.size()).successCount(successCount).sentAt(LocalDateTime.now())
                .build());

        return AdminFcmSendResultDto.builder()
                .targetType(ageGroup == null ? "ALL" : "FILTER")
                .totalTargetCount(targets.size())
                .successCount(successCount)
                .build();
    }

    /**
     * [수정] '위험군' 대상 발송 메소드
     */
    public AdminFcmSendResultDto sendNotificationToRiskGroupChildren() {
        // TODO: userRepository.findUsersWithRiskGroupChildren() 같은 최적화된 쿼리로 개선 필요
        List<Users> allUsers = userRepository.findAll();

        List<String> successfulNames = new ArrayList<>();
        List<String> failedNames = new ArrayList<>();
        List<Users> targetUsers = new ArrayList<>();

        // 1. 대상자 필터링
        for(Users user : allUsers) {
            if (user.getMember() != null &&
                    user.getMember().getChildren().stream().anyMatch(c -> Boolean.TRUE.equals(c.getRiskGroup()))) {
                targetUsers.add(user);
            }
        }

        String title = "우리아이 돌봄 특별 문진 요청";
        String link = "https://charti.site/specialSurvey";

        // 2. 발송 로직
        for(Users user : targetUsers) {
            String body = user.getName() + "님, 자녀의 건강 상태 확인을 위한 특별 문진을 작성해주세요.";
            boolean sent = sendFcmAndSaveNotice(user, title, body, FcmCategory.SPECIAL, link);

            if(sent) {
                successfulNames.add(user.getName()); // 학부모 이름으로 기록
            } else {
                failedNames.add(user.getName() + " (발송 실패)");
            }
        }

        // 3. 이력 저장
        historyRepository.save(FcmSendHistory.builder()
                .title(title).body("위험군 자녀 대상 특별문진 요청")
                .category(FcmCategory.SPECIAL).targetCondition("riskGroup=true")
                .targetCount(targetUsers.size()).successCount(successfulNames.size())
                .sentAt(LocalDateTime.now()).link(link).build());

        // 4. 결과 반환
        return AdminFcmSendResultDto.builder()
                .targetType("RISK")
                .totalTargetCount(targetUsers.size())
                .successCount(successfulNames.size())
                .successfulRecipientNames(successfulNames)
                .failedRecipientNames(failedNames)
                .build();
    }

    /**
     * [수정] '특정 기관 그룹' 대상 발송 메소드
     */
    public AdminFcmSendResultDto sendNotificationToGroupChildren(TargetGroup targetGroup) {
        // TODO: userRepository.findParentsWithChildrenInGroup(groupId) 같은 최적화된 쿼리 필요
        List<Users> allUsers = userRepository.findAll();

        List<String> successfulNames = new ArrayList<>();
        List<String> failedNames = new ArrayList<>();
        List<Users> targetUsers = new ArrayList<>();

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
            boolean sent = sendFcmAndSaveNotice(user, title, body, FcmCategory.GROUP, null);

            if(sent) {
                successfulNames.add(user.getName());
            } else {
                failedNames.add(user.getName() + " (발송 실패)");
            }
        }

        historyRepository.save(FcmSendHistory.builder()
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
     * [추가] FCM 발송 및 Notice 저장을 처리하는 private 헬퍼 메소드
     */
    private boolean sendFcmAndSaveNotice(Users user, String title, String body, FcmCategory category, String url) {
        List<UserFcmToken> tokens = tokenRepository.findByUserAndIsActiveTrue(user);
        if (tokens.isEmpty()) {
            return false;
        }

        boolean sentSuccessfully = false;
        for (UserFcmToken token : tokens) {
            try {
                // 사용하는 서비스 이름에 맞게 통일 필요
                firebaseMessagingService.sendMessageToToken(token.getFcmToken(), title, body, url);
                sentSuccessfully = true; // 한 번이라도 성공하면 true
            } catch (Exception e) {
                log.warn("FCM 발송 실패: userId={}, token={}", user.getId(), token.getFcmToken(), e);
            }
        }

        // FCM 발송에 성공한 경우에만 Notice를 저장
        if (sentSuccessfully) {
            noticeRepository.save(
                    Notice.builder()
                            .user(user)
                            .title(title)
                            .body(body)
                            .category(category)
                            .url(url)
                            .sentAt(LocalDateTime.now())
                            .build()
            );
        }
        return sentSuccessfully;
    }

    /**
     * [유지] 담당자가 소속 그룹의 학부모들에게 '자녀별'로 '문진 세트' 알림을 발송합니다.
     */
    public FcmSendResultDto sendSurveySetToGroupMembers(Long managerId, Long setId) {
        // 1. 필요한 정보 조회
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저입니다. ID: " + managerId));
        SurveySet set = surveySetService.getById(setId);
        List<Child> targetChildren = childRepository.findByGroupId(manager.getGroup().getId());

        if (targetChildren.isEmpty()) {
            log.info("그룹 ID {} 에 해당하는 자녀가 없어 알림을 발송하지 않습니다.", manager.getGroup().getId());
            return new FcmSendResultDto(0, 0, new ArrayList<>(), new ArrayList<>());
        }

        LocalDateTime sentTime = LocalDateTime.now();
        List<Notice> noticesToSave = new ArrayList<>();
        List<String> successfulNames = new ArrayList<>();
        List<String> failedNames = new ArrayList<>();

        for (Child child : targetChildren) {
            Users parent = child.getParent().getUsers();
            if (parent == null || parent.isDeleted()) {
                failedNames.add(child.getName() + " (학부모 정보 없음)");
                continue;
            }

            String title = String.format("[문진 요청] %s 어린이를 위한 새 문진이 도착했어요!", child.getName());
            String body = String.format("'%s' 문진을 확인하고 답변을 부탁드립니다.", set.getSetTitle());
            String link = "https://charti.site/surveySet/request/" + setId + "?childId=" + child.getId();

            // private 헬퍼 메소드 사용
            boolean sent = sendFcmAndSaveNotice(parent, title, body, FcmCategory.GROUP, link);

            if (sent) {
                successfulNames.add(child.getName());
            } else {
                failedNames.add(child.getName() + " (발송 실패)");
            }
        }

        // 담당자용 발송 이력 저장
        historyRepository.save(
                FcmSendHistory.builder()
                        .sender(manager.getUsers())
                        .title(set.getSetTitle())
                        .body(String.format("%s 담당자가 그룹 문진을 발송했습니다.", manager.getUsers().getName()))
                        .category(FcmCategory.GROUP)
                        .targetCondition("groupId=" + manager.getGroup().getId())
                        .link("https://charti.site/surveySet/request/" + setId)
                        .targetCount(targetChildren.size())
                        .successCount(successfulNames.size())
                        .sentAt(sentTime)
                        .build()
        );

        return new FcmSendResultDto(targetChildren.size(), successfulNames.size(), successfulNames, failedNames);
    }
}
