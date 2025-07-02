package com.example.demo.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class SpecialSurveyRequestDto {
    private Long childId;       // 추가: 자녀 ID
    private String ageGroup;    // 연령대
    private String category; // 대상 그룹
    private Long setId; // 세트 아이디

    // SurveyId와 answerValue를 함께 담는 내부 DTO
    private List<AnswerDto> answers;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class AnswerDto {
        private Long surveyId;              // SpecialSurvey PK
        private Long surveySetId;           // SpecialSurvey가 속한 SurveySet PK
        private String question;            // 문항 텍스트
        private String answerText;          // 사용자가 고른 답변(문자열)

        public AnswerDto(SurveySetSubmitRequestDto.AnswerDto answerDto) {
        }
    }
}