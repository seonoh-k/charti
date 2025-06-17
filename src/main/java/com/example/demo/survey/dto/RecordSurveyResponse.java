package com.example.demo.survey.dto;

import com.example.demo.enums.AgeGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecordSurveyResponse {
    private Long id;
    private String question;
    private AgeGroup ageGroup;
    private LocalDateTime createdAt;
}