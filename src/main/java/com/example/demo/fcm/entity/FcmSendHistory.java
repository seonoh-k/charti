package com.example.demo.fcm.entity;

import com.example.demo.enums.FcmCategory;
import com.example.demo.users.entity.Admin;
import com.example.demo.users.entity.Users;
import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import jakarta.persistence.Transient;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;


/**
 * FCM 발송 이력 저장 엔티티
 * 누가 어떤 메시지를 누구에게 보냈는지 기록
 */

@Entity
@Table(name = "fcm_send_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmSendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림 제목
     */
    private String title;

    /**
     * 알림 내용 (본문)
     */
    private String body;

    /**
     * 알림 카테고리: DAILY(데일리), SPECIAL(특별문진), NOTICE(공지) 등
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FcmCategory category;

    /**
     * 발송 대상 조건 (예: "age=1~2,notCompletedDaily=true")
     */
    @Column(length = 1024)
    private String targetCondition;

    /**
     * 실제 발송 대상자 수
     */
    private int targetCount;

    /**
     * FCM 전송에 성공한 수신자 수
     */
    private int successCount;

    /**
     * 알림 발송 일시
     */
    private LocalDateTime sentAt;

    /**
     * 알림을 보낸 관리자 (users 테이블 참조)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Users sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_sender_id")
    private Admin adminSender;

    /**
     *  알림을 보낸 주체의 이름을 반환하는 헬퍼 메소드.
     * 관리자가 보냈으면 관리자 이름을, 담당자가 보냈으면 담당자 이름을 반환합니다.
     */
    @Transient
    public String getSenderName() {
        if (this.adminSender != null) {
            // 관리자가 보낸 경우
            return this.adminSender.getName();
        } else if (this.sender != null) {
            // 담당자 또는 일반 사용자가 보낸 경우
            return this.sender.getName();
        } else {
            // 둘 다 없는 예외적인 경우 (또는 시스템이 직접 보낸 경우)
            return "시스템";
        }
    }

    /**
     * [이동 링크]
     * 이 알림을 클릭했을 때 이동하게 될 공통 URL.
     * 예: 특정 문진 세트 응답 페이지 (/survey/set/123 등)
     *
     * 수신자 개별 알림(Notice.url)과는 다르게,
     * 발송 기록 차원에서 '어떤 링크로 안내했는가'를 남기기 위해 사용함.
     */
    @Column(name = "link", length = 1024)
    private String link;

    /**
     *  targetCondition 문자열을 사람이 읽기 좋은 형태로 변환하여 반환합니다.
     */
    @Transient
    public String getDisplayCondition() {
        if (this.targetCondition == null || this.targetCondition.isBlank()) {
            return "전체 대상";
        }

        Map<String, String> conditions = Arrays.stream(this.targetCondition.split(","))
                .map(s -> s.split("=", 2))
                .filter(arr -> arr.length == 2 && !arr[1].equals("null") && !arr[1].isBlank())
                .collect(Collectors.toMap(
                        arr -> arr[0].trim(),
                        arr -> arr[1].trim()
                ));
        if (conditions.containsKey("userId")) {
            return "특정 사용자 지정";
        }

        String targetType = conditions.getOrDefault("target", "").trim();

        StringBuilder displayText = new StringBuilder();
        switch (targetType) {
            case "SPECIAL_RISK":
                displayText.append("위험군 대상");
                break;
            case "GROUP":
                try {
                    String groupName = conditions.getOrDefault("targetGroup", "");
                    TargetGroup tg = TargetGroup.valueOf(groupName);
                    displayText.append(tg.getDisplayName()).append(" 그룹");
                } catch (Exception e) {
                    displayText.append("특정 그룹");
                }
                break;
            case "ALL":
                displayText.append("전체(필터) 대상");
                break;
            default:
                return this.targetCondition;
        }

        List<String> filters = new ArrayList<>();
        if (conditions.containsKey("ageGroup")) {
            try {
                AgeGroup ag = AgeGroup.valueOf(conditions.get("ageGroup"));
                filters.add(ag.getDisplayName());
            } catch (Exception e) { /* 무시 */ }
        }
        if (conditions.containsKey("category")) {
            try {
                SurveyCategory sc = SurveyCategory.valueOf(conditions.get("category"));
                filters.add(sc.getDisplayName());
            } catch (Exception e) { /* 무시 */ }
        }

        if (!filters.isEmpty()) {
            displayText.append(" (").append(String.join(", ", filters)).append(")");
        }

        return displayText.toString();
    }
}