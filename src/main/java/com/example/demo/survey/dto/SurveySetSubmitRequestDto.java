package com.example.demo.survey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveySetSubmitRequestDto {
    private Long childId;
    private Long setId;
    private List<Integer> answers;

    private String ageGroup;    // 연령대
    private String category; // 대상 그룹

    // SurveyId와 answerValue를 함께 담는 내부 DTO
    private List<SurveySetSubmitRequestDto.AnswerDto> answerList;

    @Getter @Setter
    public static class AnswerDto {
        private Long surveyId;              // SpecialSurvey PK
        private String question;
        private Integer answerValue;          // 사용자가 고른 답변(문자열)
    }
}
