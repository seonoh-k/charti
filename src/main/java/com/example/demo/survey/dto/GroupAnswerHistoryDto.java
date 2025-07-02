package com.example.demo.survey.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 이력 페이지 최종 응답을 위한 DTO
 * - 평가 결과와 개별 답변 목록을 모두 포함
 */
@Getter
@Builder
public class GroupAnswerHistoryDto {
    private String completedAt;
    private String ageGroup;
    private String targetGroup;
    private Map<String, Object> evaluationResult; // GroupSurveyService.evaluate()의 결과
    private List<GroupAnswerDto> individualAnswers; // 기존의 상세 답변 목록
}