package com.example.demo.survey.dto;

import com.example.demo.survey.entity.SpecialAnswer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class SpecialAnswerDto {
    private Long id;
    private String childDisplay;
    private String category;
    private String ageGroup;
    private String question;
    private String answer;
    private LocalDateTime createdAt;
    private List<String> possibleAnswers;
    private int selectedValue;

    public static SpecialAnswerDto fromEntity(SpecialAnswer a) {
        SpecialAnswerDto d = new SpecialAnswerDto();
        d.setId(a.getId());
        var c = a.getChild();
        var age = c.getBirthday().toLocalDate()
                .until(LocalDate.now()).getYears();
        d.setChildDisplay(c.getName()+"("+c.getNickname()+") - "+age+"세");
        d.setCategory(a.getCategory().getDisplayName());
        d.setAgeGroup(a.getAgeGroup().getDisplayName());
        d.setQuestion(a.getQuestion());
        d.setAnswer(a.getAnswer());
        d.setCreatedAt(a.getCreatedAt());

        var s = a.getSurvey();
        List<String> opts = List.of(
                s.getAnswer1(), s.getAnswer2(),
                s.getAnswer3(), s.getAnswer4(), s.getAnswer5()
        );
        d.setPossibleAnswers(opts);
        d.setSelectedValue(opts.indexOf(a.getAnswer())+1);
        return d;
    }
}