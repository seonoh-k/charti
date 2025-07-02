package com.example.demo.survey.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GroupSurveyRequestDto {
    private Long childId;       // 추가: 자녀 ID
    private String ageGroup;    // 연령대
    private String targetGroup; // 대상 그룹
    private Long setId; // 세트 아이디
    private List<Integer> answers; // 문항별 응답 값(1~5)
}