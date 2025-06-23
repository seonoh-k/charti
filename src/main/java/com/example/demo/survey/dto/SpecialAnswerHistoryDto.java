package com.example.demo.survey.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class SpecialAnswerHistoryDto {
    private String completedAt;
    private String ageGroup;
    private String category;
    private Map<String, Object> evaluationResult;
    private List<SpecialAnswerDto> individualAnswers;
}