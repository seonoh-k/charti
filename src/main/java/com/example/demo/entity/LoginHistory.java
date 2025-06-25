package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;          // Email
    private Long userId;              // 로그인한 사용자 PK
    private LocalDateTime timestamp;  // 시각
    private String ipAddress;         // 요청 IP
    private boolean success;          // 성공 여부

    private String failureReason;     // 실패 시 원인 (optional)
}
