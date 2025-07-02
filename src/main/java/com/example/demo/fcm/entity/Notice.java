package com.example.demo.fcm.entity;

import com.example.demo.users.entity.Users;
import com.example.demo.enums.FcmCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    /** 알림 수신 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    /** 알림 제목/본문/카테고리/발송시각 */
    private String title;
    private String body;

    @Enumerated(EnumType.STRING)
    private FcmCategory category;

    private LocalDateTime sentAt;

    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean readFlag = false;

    /** 버튼으로 이동할 링크 */
    @Column(length = 512)
    private String url;

    /** soft delete */
    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDateTime deletedAt;


}
