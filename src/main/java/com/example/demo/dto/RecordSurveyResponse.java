package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecordSurveyResponse {
    private Long id;
    private String question;
    private LocalDateTime createdAt;
}
