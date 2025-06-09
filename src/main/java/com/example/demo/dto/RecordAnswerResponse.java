package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecordAnswerResponse {
    private Long answerId;
    private String question;
    private String answer;
    private String childName;
    private LocalDateTime createdAt;
}
