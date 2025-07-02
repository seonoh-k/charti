package com.example.demo.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class SurveySetDTO {
    private Long setId;
    private List<SpecialSurveyResponseDto> surveyList;
}
