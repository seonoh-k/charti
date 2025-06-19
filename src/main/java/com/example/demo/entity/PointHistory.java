package com.example.demo.entity;

import com.example.demo.enums.PointType;
import com.example.demo.users.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "point_history")
@Getter
@Setter
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Member member;

    @Column(name = "change_amount", nullable = false)
    private int changeAmount;

    @Column(name = "description", length = 255)
    private String description;

    // 포인트 지급 유형 (예: RECORD_SURVEY, DAILY_SURVEY 등)
    // 중복 지급 방지 및 지급 내역 구분을 위해 사용
    @Enumerated(EnumType.STRING)
    @Column(name = "point_type")
    private PointType pointType;

    // 포인트 지급 대상 자녀 이름
    // 동일 보호자 계정 아래 여러 자녀가 있을 경우 자녀별 지급 여부 판별 용도
    @Column(name = "child_name")
    private String childName;

    // 포인트 지급 날짜
    // 1일 1회 지급 제한을 체크하기 위해 날짜 기준으로 중복 여부 확인
    @Column(name = "point_date")
    private LocalDate pointDate;

}
