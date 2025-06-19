package com.example.demo.survey.dto;

import com.example.demo.enums.AgeGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 기록 문진 질문 정보를 클라이언트에게 반환할 때 사용되는 DTO입니다.
 * 주로 문진 질문 목록을 조회하거나 특정 질문의 상세 정보를 보여줄 때 사용됩니다.
 */
@Getter
@AllArgsConstructor
public class RecordSurveyResponse {
    /**
     * 문진 질문의 고유 ID입니다.
     */
    private Long id;
    /**
     * 문진 질문의 내용입니다.
     */
    private String question;
    /**
     * 이 문진 질문이 속하는 연령대입니다.
     */
    private AgeGroup ageGroup;
    /**
     * 문진 질문이 생성된 일시입니다.
     */
    private LocalDateTime createdAt;
}