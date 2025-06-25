package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SpecialSurveyRequestDto {
    private Long childId;       // 추가: 자녀 ID
    private String ageGroup;    // 연령대
    private String category; // 대상 그룹
    private Long setId; // 세트 아이디
    private List<Integer> answers; // 문항별 응답 값(1~5)
}