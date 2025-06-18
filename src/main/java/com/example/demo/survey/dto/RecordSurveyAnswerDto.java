package com.example.demo.survey.dto;

import lombok.Data;

@Data
public class RecordSurveyAnswerDto {
    private Long questionId;   // 질문 ID
    private String text;       // 사용자가 입력한 답변
}