package com.example.demo.survey.dto;

import com.example.demo.survey.entity.SpecialSurvey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
public class SpecialSurveyResponseDto {
    private Long id;
    private String question;
    private String answer1;
    private String answer2;
    private String answer3;
    private String answer4;
    private String answer5;
    private String category;
    private String ageGroup;

    // 문항이 속한 세트 ID
    private Long surveySetId;

    public static SpecialSurveyResponseDto fromEntity(SpecialSurvey e) {
        SpecialSurveyResponseDto dto = new SpecialSurveyResponseDto();
        dto.setId(e.getId());
        dto.setQuestion(e.getQuestion());
        dto.setAnswer1(e.getAnswer1());
        dto.setAnswer2(e.getAnswer2());
        dto.setAnswer3(e.getAnswer3());
        dto.setAnswer4(e.getAnswer4());
        dto.setAnswer5(e.getAnswer5());
        dto.setCategory(e.getCategory().getDisplayName());
        dto.setAgeGroup(e.getAgeGroup().getDisplayName());

        // **SurveySet 과 N:M 이라면**, 편의상 첫번째 set 을 내려주도록
        if (!e.getSurveySets().isEmpty()) {
            dto.setSurveySetId(e.getSurveySets().iterator().next().getSetId());
        }
        return dto;
    }
}