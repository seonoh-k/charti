package com.example.demo.fcm.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자별 FCM 토큰 저장 엔티티
 * 하나의 유저가 여러 디바이스/브라우저에서 로그인할 수 있으므로
 * 다대일 관계로 구성
 */
@Entity
@Table(name = "user_fcm_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연결된 유저 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    // 유저의 디바이스별 토큰
    @Column(name = "fcm_token", nullable = false, length = 512, unique = true)
    private String fcmToken;

    // 마지막으로 사용된 시간
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // 토큰이 비활성화되었는지 여부
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
