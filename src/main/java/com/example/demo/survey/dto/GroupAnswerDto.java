package com.example.demo.survey.dto;

import com.example.demo.survey.entity.GroupAnswer;
import com.example.demo.survey.entity.GroupSurvey;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 이력 API 응답용 DTO */
@Getter @Setter
public class GroupAnswerDto {
    private Long id;
    private String childDisplay;
    private String category;
    private String ageGroup;
    private String targetGroup;
    private String question;
    private String answer;
//    private Integer weight;
    private LocalDateTime createdAt;

    // 버튼 모달용 옵션 목록
    private List<String> possibleAnswers;
    // 현재 선택된 답 (1~5)
    private int selectedValue;

    public static GroupAnswerDto fromEntity(GroupAnswer a) {
        GroupAnswerDto d = new GroupAnswerDto();
        d.setId(a.getId());
        // 자녀 표시
        var c   = a.getChild();
        var age = c.getBirthday().toLocalDate()
                .until(LocalDate.now()).getYears();
        d.setChildDisplay(c.getName()+"("+c.getNickname()+") - "+age+"세");

        d.setCategory(a.getCategory().getDisplayName());
        d.setAgeGroup(a.getAgeGroup().getDisplayName());
        d.setTargetGroup(a.getTargetGroup().getDisplayName());
        d.setQuestion(a.getQuestion());
        d.setAnswer(a.getAnswer());
        d.setCreatedAt(a.getCreatedAt());

        // 1 SurveySet에서 question이 같은 GroupSurvey 객체 찾기
        GroupSurvey s = a.getSurveySet().getGroupSurveys().stream()
                .filter(gs -> gs.getQuestion().equals(a.getQuestion()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("문진 항목 없음: " + a.getQuestion()));

        // 2 그 객체에서 옵션 꺼내기
        List<String> opts = List.of(
                s.getAnswer1(), s.getAnswer2(),
                s.getAnswer3(), s.getAnswer4(), s.getAnswer5()
        );
        d.setPossibleAnswers(opts);
        d.setSelectedValue(opts.indexOf(a.getAnswer()) + 1);

        return d;
    }
}
