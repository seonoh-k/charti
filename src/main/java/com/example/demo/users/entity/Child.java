package com.example.demo.users.entity;

import com.example.demo.enums.AgeGroup;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "child")
@Getter
@Setter
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "child_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    @JsonBackReference
    private Member parent;  // 부모 회원 참조

    @Column(nullable = false)
    private String name;

    private String nickname;
    private LocalDateTime birthday;
    private String weight;
    private String height;
    private String gender;
    private Integer birthOrder;
    private Boolean riskGroup;


    /**
     * 자녀의 생일을 기준으로 연령대를 계산하여 {@link AgeGroup} 열거형으로 반환
     * 이 메서드는 DB에 저장되지 않는 계산용 메서드로, 연령대 필터링 및 FCM 발송 조건 등에 활용
     *
     * @return AgeGroup 열거형 값 (예: AGE_0_12, AGE_1_2 등), 생일이 없을 경우 null 반환
     */
    @Transient
    public AgeGroup getAgeGroup() {
        // 생일 정보가 없으면 연령대 계산 불가
        if (this.birthday == null) return null;

        // 생일부터 현재까지의 개월 수를 계산
        long months = ChronoUnit.MONTHS.between(this.birthday, LocalDateTime.now());

        // 개월 수를 기준으로 AgeGroup enum 값 반환
        if (months <= 12) {
            return AgeGroup.AGE_0_12;       // 0~12개월
        } else if (months <= 24) {
            return AgeGroup.AGE_1_2;        // 1~2세
        } else if (months <= 48) {
            return AgeGroup.AGE_3_4;        // 3~4세
        } else {
            return AgeGroup.AGE_5;          // 5세 이상
        }
    }
    /**
     * 생년월일을 기준으로 만 나이를 계산하여 반환합니다.
     * Thymeleaf에서 ${child.age}로 편리하게 사용하기 위함입니다.
     */
    public int getAge() {
        if (this.birthday == null) return 0;
        LocalDate today = LocalDate.now();
        return Period.between(this.birthday.toLocalDate(), today).getYears();
    }



}
