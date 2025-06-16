package com.example.demo.survey.dto;

import com.example.demo.survey.entity.SpecialSurvey;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class SpecialSurveyResponseDto {
    private Long id;
    private String question;
    private String category;
    private String ageGroup;
    private LocalDate surveyDate;
    private int weight;
    private String answer1, answer2, answer3, answer4, answer5;
    private String selectedAnswer;
    private int calculatedScore;
    private List<String> surveySetTitles;

    public static SpecialSurveyResponseDto fromEntity(SpecialSurvey entity) {
        return SpecialSurveyResponseDto.builder()
                .id(entity.getId())
                .question(entity.getQuestion())
                .category(entity.getCategory())
                .ageGroup(entity.getAgeGroup())
                .surveyDate(entity.getSurveyDate())
                .weight(entity.getWeight())
                .answer1(entity.getAnswer1())
                .answer2(entity.getAnswer2())
                .answer3(entity.getAnswer3())
                .answer4(entity.getAnswer4())
                .answer5(entity.getAnswer5())
                .selectedAnswer(entity.getSelectedAnswer())
                .calculatedScore(entity.getCalculatedScore())
                .surveySetTitles(entity.getSurveySets().stream()
                        .map(set -> set.getSetTitle())
                        .collect(Collectors.toList()))
                .build();
    }
}
