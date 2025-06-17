package com.example.demo.survey.dto;

import com.example.demo.enums.AgeGroup;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RecordSurveyRequest {
    private AgeGroup ageGroup;
    private String question;
}