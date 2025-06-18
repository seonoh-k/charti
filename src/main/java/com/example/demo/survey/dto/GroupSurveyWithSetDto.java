package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupSurveyWithSetDto extends GroupSurveyRequestDto {
    private Long surveySetId;
}