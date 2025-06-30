package com.example.demo.users.entity;

import com.example.demo.entity.Group;
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

    private Boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;


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

        // 만 나이(년) 계산
        int years = Period.between(this.birthday.toLocalDate(), LocalDate.now()).getYears();

        // 연 단위로 분류
        if (years < 1) {
            return AgeGroup.AGE_0_12;       // 0~12개월
        } else if (years <= 2) {
            return AgeGroup.AGE_1_2;        // 1~2세
        } else if (years <= 4) {
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


    /**
     * 몇 개월·몇 세인지 문자열로 보여주는 헬퍼
     * 템플릿에서 ${c.ageDisplay} 로 사용
     */
    @Transient
    public String getAgeDisplay() {
        if (birthday == null) return "";
        Period p = Period.between(birthday.toLocalDate(), LocalDate.now());
        if (p.getYears() == 0) {
            return p.getMonths() + "개월";   // ex. "7개월"
        }
        return p.getYears() + "세";         // ex. "2세"
    }


}
