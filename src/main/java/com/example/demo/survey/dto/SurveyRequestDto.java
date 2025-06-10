package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class SurveyRequestDto {
    private String ageGroup;
    private List<Integer> answers;
}
