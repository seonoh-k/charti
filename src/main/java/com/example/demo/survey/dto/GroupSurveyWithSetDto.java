package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List; // List를 사용하므로 import가 필요합니다.

@Getter
@Setter
public class GroupSurveyWithSetDto extends GroupSurveyRequestDto {

    private Long surveySetId;


    public GroupSurveyWithSetDto(Long childId, String ageGroup, String targetGroup, List<Integer> answers, Long surveySetId) {
        // 1. 부모 클래스(GroupSurveyRequestDto)의 생성자를 명시적으로 호출
        super(childId, ageGroup, targetGroup, answers);

        // 2. 자식 클래스(GroupSurveyWithSetDto) 자신의 필드를 초기화
        this.surveySetId = surveySetId;
    }
}