package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;

import com.example.demo.users.entity.Child;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "special_survey")
@Getter
@Setter
public class SpecialSurvey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String category;

    @ManyToMany
    @JoinTable(
            name = "special_survey_set", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "special_survey_id"), // 현재 엔티티의 FK
            inverseJoinColumns = @JoinColumn(name = "survey_set_id") // 반대편 FK
    )
    private List<SurveySet> surveySets;

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;


    @Column(nullable = false)
    private String answer1;

    @Column(nullable = false)
    private String answer2;

    @Column
    private String answer3;

    @Column
    private String answer4;

    @Column
    private String answer5;

    @Column(name = "selected_answer", nullable = false)
    private String selectedAnswer;

    @Column(name = "calculated_score", nullable = false)
    private int calculatedScore;


}
