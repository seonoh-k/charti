package com.example.demo.dto;

import com.example.demo.enums.AgeGroup;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildDTO {

    private Long id;

    private Integer age;
    private Integer birthOrder;

    private String name;
    private String nickname;
    private String weight;
    private String height;
    private String gender;

    private Boolean riskGroup;

    private LocalDateTime birthday;

    public static ChildDTO fromEntity(Child child) {
        return ChildDTO.builder()
                .id(child.getId())
                .age(child.getAge())
                .birthOrder(child.getBirthOrder())
                .name(child.getName())
                .nickname(child.getNickname())
                .weight(child.getWeight())
                .height(child.getHeight())
                .gender(child.getGender())
                .riskGroup(child.getRiskGroup())
                .birthday(child.getBirthday())
                .build();
    }

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
    public int calculateAge() {
        if (this.birthday == null) return 0;
        LocalDate today = LocalDate.now();
        return Period.between(this.birthday.toLocalDate(), today).getYears();
    }


    public ChildDTO(Long id, Integer birthOrder, String name, String nickname,
                    String weight, String height, String gender, Boolean riskGroup,
                    LocalDateTime birthday) {
        this.id = id;
        this.birthOrder = birthOrder;
        this.name = name;
        this.nickname = nickname;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
        this.riskGroup = riskGroup;
        this.birthday = birthday;
        this.age = calculateAge();
    }
}

