package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RecordAnswerRequest {
    private Long surveyId;
    private Long childId;
    private String answer;
}
