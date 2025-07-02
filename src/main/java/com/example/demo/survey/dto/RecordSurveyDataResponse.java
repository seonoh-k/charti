package com.example.demo.survey.dto;

import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.users.entity.Child;
import lombok.AllArgsConstructor; // 생성자 자동 생성을 위해 추가 (명시적 생성자 선언 대신)
import lombok.Getter;

import java.util.List;

/**
 * 특정 자녀에게 제시될 기록 문진 질문 목록과 해당 자녀의 정보를 함께 반환하는 DTO입니다.
 * 주로 문진 작성 페이지를 로드할 때 사용되어, 어떤 자녀에 대해 어떤 질문들을 보여줄지 결정합니다.
 */
@Getter
// @AllArgsConstructor // Lombok으로 생성자를 자동 생성할 경우 주석 처리하거나, 직접 정의한 생성자를 유지
public class RecordSurveyDataResponse {

    /**
     * 문진 질문을 받을 자녀의 정보입니다.
     */
    private final Child child;
    /**
     * 해당 자녀에게 제시될 기록 문진 질문들의 리스트입니다.
     */
    private final List<RecordSurveyResponse> surveys;

    /**
     * RecordSurveyDataResponse의 생성자입니다.
     *
     * @param child 문진 질문을 받을 자녀 엔티티
     * @param surveys 자녀에게 제시될 RecordSurvey 엔티티 리스트
     */
//    public RecordSurveyDataResponse(Child child, List<RecordSurvey> surveys) {
//        this.child = child;
//        this.surveys = surveys;
//    }

    public RecordSurveyDataResponse(Child child, List<RecordSurveyResponse> surveys) {
        this.child = child;
        this.surveys = surveys;
    }
}