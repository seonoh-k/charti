package com.example.demo.survey.dto;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DailyAnswerDto {
    private Long           id;
    private String         childName;
    private String         category;
    private String         ageGroup;
    private String         question;
    private String         answer;
    private Integer         weight;
    private LocalDateTime  createdAt;
}