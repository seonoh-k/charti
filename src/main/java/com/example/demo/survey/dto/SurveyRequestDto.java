package com.example.demo.survey.dto;

import com.example.demo.enums.AgeGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class SurveyRequestDto {
    private AgeGroup ageGroup;
    private Long childId;
    private List<Integer> answers;
}
