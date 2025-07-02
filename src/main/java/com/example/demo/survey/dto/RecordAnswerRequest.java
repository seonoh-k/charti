package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 기록 문진 답변을 생성하거나 업데이트할 때 클라이언트로부터 받는 요청 데이터를 담는 DTO입니다.
 * 주로 단일 질문에 대한 답변 제출 시 사용될 수 있습니다.
 */
@Getter @Setter
public class RecordAnswerRequest {
    /**
     * 답변하고자 하는 문진 질문의 고유 ID입니다. (RecordSurvey 엔티티의 ID)
     */
    private Long surveyId;
    /**
     * 답변을 작성하는 자녀의 고유 ID입니다.
     */
    private Long childId;
    /**
     * 사용자가 입력한 답변 내용입니다.
     */
    private String answer;
}