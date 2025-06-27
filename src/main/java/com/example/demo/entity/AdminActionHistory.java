package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;           // 관리자 식별자

    private String adminName;       // ← 추가된 관리자 이름은 같을 수 있음

    private String httpMethod;

    @Column(nullable = false)
    private String category;            // @AdminAuditLog.category()

    @Column(nullable = false, length = 2048)
    private String path;                // 요청 URL

    @Column(length = 2048)
    private String params;              // 쿼리스트링 또는 바디

    @Column(length = 2048)
    private String response;            // 응답 본문 또는 예외 메시지

    private Integer status;             // HTTP 상태 코드

    @Column(nullable = false)
    private LocalDateTime requestDate;  // 요청 시각

    @Column(nullable = false)
    private LocalDateTime responseDate; // 응답 완료 시각
}
