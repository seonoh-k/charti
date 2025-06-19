package com.example.demo.survey.dto;

import com.example.demo.enums.AgeGroup;
import lombok.Getter;
import lombok.Setter;

/**
 * 새로운 기록 문진 질문을 생성할 때 클라이언트로부터 받는 요청 데이터를 담는 DTO입니다.
 * 주로 관리자 페이지에서 문진 질문을 추가할 때 사용됩니다.
 */
@Getter @Setter
public class RecordSurveyRequest {
    /**
     * 해당 문진 질문이 적용될 연령대입니다. (예: AgeGroup.CHILD, AgeGroup.TEENAGER)
     */
    private AgeGroup ageGroup;
    /**
     * 문진 질문의 실제 내용입니다. (예: "오늘 하루 기분은 어떠했나요?")
     */
    private String question;
}