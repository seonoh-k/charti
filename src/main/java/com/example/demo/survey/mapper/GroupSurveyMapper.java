package com.example.demo.survey.mapper;

import com.example.demo.survey.dto.GroupSurveyResponseDto;
import com.example.demo.survey.entity.GroupSurvey;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GroupSurveyMapper {

    public GroupSurveyResponseDto toDto(GroupSurvey gs) {
        return GroupSurveyResponseDto.builder()
                .id(gs.getId())
                .ageGroup(gs.getAgeGroup())
                .question(gs.getQuestion())
                .category(gs.getCategory())
                .surveyDate(gs.getSurveyDate())
                .targetGroup(gs.getTargetGroup())
                .weight(gs.getWeight())
                .answer1(gs.getAnswer1())
                .answer2(gs.getAnswer2())
                .answer3(gs.getAnswer3())
                .answer4(gs.getAnswer4())
                .answer5(gs.getAnswer5())
                .selectedAnswer(gs.getSelectedAnswer())
                .calculatedScore(gs.getCalculatedScore())
                .surveySetTitles(
                        gs.getSurveySets().stream()
                                .map(set -> set.getSetTitle())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
