package com.example.demo.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 기록 문진 답변 수정 요청 시 사용하는 DTO.
 * - 자녀 ID와 질문 ID를 기반으로 특정 답변을 찾아 수정할 때 사용됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAnswerRequestDto {

    /**
     * 수정 대상 질문의 ID (RecordSurvey.surveyId).
     */
    private Long questionId;

    /**
     * 수정된 답변 내용.
     */
    private String answer;

    /**
     * 답변이 속한 자녀의 ID.
     */
    private Long childId;
}
