package com.example.demo.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 특정 날짜에 대한 기록 문진의 질문과 그에 대한 답변을 한 쌍으로 묶어서 표현하는 DTO입니다.
 * 주로 관리자 페이지에서 특정 문진 기록의 상세 내용을 조회할 때 사용됩니다.
 */
@Getter
@AllArgsConstructor
public class QuestionAnswerPairDto {
    /**
     * 문진 질문의 내용입니다.
     */
    private String question;
    /**
     * 해당 질문에 대한 사용자의 답변 내용입니다.
     */
    private String answer;
}