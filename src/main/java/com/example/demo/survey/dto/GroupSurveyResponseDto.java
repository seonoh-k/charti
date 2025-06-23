package com.example.demo.survey.dto;

import com.example.demo.survey.entity.GroupSurvey;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class GroupSurveyResponseDto {
    private Long id;
    private String question;
    private String category;      // ENUM -> String
    private String ageGroup;      // ENUM -> String
    private String targetGroup;   // ENUM -> String (nullable)
//    private int weight;         // 제거
    private String answer1, answer2, answer3, answer4, answer5;
    private List<String> surveySetTitles;

    public static GroupSurveyResponseDto fromEntity(GroupSurvey entity) {
        return GroupSurveyResponseDto.builder()
                .id(entity.getId())
                .question(entity.getQuestion())
                .category(entity.getCategory().getDisplayName())           // enum -> String
                .ageGroup(entity.getAgeGroup().getDisplayName())           // enum -> String
                .targetGroup(entity.getTargetGroup()                       // Optional 처리
                        .map(tg -> tg.getDisplayName())
                        .orElse(null))
//                .weight(entity.getWeight())     //제거
                .answer1(entity.getAnswer1())
                .answer2(entity.getAnswer2())
                .answer3(entity.getAnswer3())
                .answer4(entity.getAnswer4())
                .answer5(entity.getAnswer5())
                .surveySetTitles(
                        entity.getSurveySets().stream()
                                .map(set -> set.getSetTitle())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
