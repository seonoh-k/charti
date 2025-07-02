package com.example.demo.dto;

import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.dto.DailyAnswerDto;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.users.entity.Child;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    private String groupName;
    private String groupEmail;
    private String groupPhone;
    private String targetGroup; // 타겟 그룹명
    private String ageDisplay;  // "4세" 같은 나이 표기
    private Long groupId;

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
    public static ChildDTO fromEntityWithDetails(Child child) {
        ChildDTO dto = fromEntity(child); // 기존 fromEntity 호출해서 기본값 세팅

        // 상세 정보 필드 추가로 세팅
        if (child.getGroup() != null) {
            dto.setGroupName(child.getGroup().getGroupName());
            dto.setGroupEmail(child.getGroup().getGroupEmail());
            dto.setGroupPhone(child.getGroup().getGroupPhoneNumber());
            dto.setGroupId(child.getGroup().getId());
            if (child.getGroup().getTargetGroup() != null) {
                dto.setTargetGroup(child.getGroup().getTargetGroup().getDisplayName());
            }
        }
        dto.setAgeDisplay(child.getAgeDisplay());
        // 필요하면 더 추가...
        return dto;
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

    public ChildDTO(Long id, Integer birthOrder, String name,
                    String nickname, String weight, String height,
                    String gender, Boolean riskGroup, String groupName,
                    String groupEmail, String groupPhone, LocalDateTime birthday) {

        this.id = id;
        this.birthOrder = birthOrder;
        this.name = name;
        this.nickname = nickname;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
        this.riskGroup = riskGroup;
        this.groupName = groupName;
        this.groupEmail = groupEmail;
        this.groupPhone = groupPhone;
        this.birthday = birthday;
        this.age = calculateAge();
    }

    private String birthdate;
    private List<DailyAnswerDto> dAnswers;
    private List<RecordAnswerResponse> rAnswers;
    public ChildDTO(Child child, List<DailyAnswerDto> dAnswers, List<RecordAnswerResponse> rAnswers) {
        this.id = child.getId();
        this.birthOrder = child.getBirthOrder();
        this.name = child.getName();
        this.nickname = child.getNickname();
        this.weight = child.getWeight();
        this.height = child.getHeight();
        this.gender = child.getGender();
        this.groupName = child.getGroup() != null ? child.getGroup().getGroupName() : null;
        this.groupPhone = child.getGroup() != null ? child.getGroup().getGroupPhoneNumber() : null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.birthdate = child.getBirthday().format(formatter);
        this.age = calculateAge();
        this.dAnswers = dAnswers;
        this.rAnswers = rAnswers;
    }
}

