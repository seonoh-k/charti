package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupSurveyRequestDto {
    private String ageGroup;
    private String targetGroup;
    private List<Integer> answers;
}