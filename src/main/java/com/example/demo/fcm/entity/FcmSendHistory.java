package com.example.demo.fcm.entity;

import com.example.demo.enums.FcmCategory;
import com.example.demo.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;

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
}