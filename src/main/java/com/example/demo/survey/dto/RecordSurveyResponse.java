package com.example.demo.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecordSurveyResponse {
    private Long id;
    private String question;
    private String ageGroup;
    private LocalDateTime createdAt;
}
