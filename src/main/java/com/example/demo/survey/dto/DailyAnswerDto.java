package com.example.demo.survey.dto;

import com.example.demo.survey.entity.DailyAnswer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private String created;

    private List<String> possibleAnswers;  // survey.answer1~answer5
    private Integer        selectedValue;    // 1~5 중 현재 답변

    // 일반 회원의 마이페이지에서 데일리 문진 이력을 간략히 조회
    public DailyAnswerDto(DailyAnswer dailyAnswer) {
        this.id = dailyAnswer.getId();
        this.childName = dailyAnswer.getChild().getName();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.created = dailyAnswer.getCreatedAt().format(formatter);
    }

    public DailyAnswerDto(Long id, String childDisplay, String displayName,
                          String displayName1, String question, String answer,
                          Integer weight, LocalDateTime createdAt, List<String> opts, int sel) {
        this.id = id;
        this.childName = childDisplay;
        this.category = displayName;
        this.ageGroup = displayName1;
        this.question = question;
        this.answer = answer;
        this.weight = weight;
        this.createdAt = createdAt;
        this.possibleAnswers = opts;
        this.selectedValue = sel;
    }
}