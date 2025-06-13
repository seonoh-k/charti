package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;

import com.example.demo.users.entity.Child;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "special_survey")
@Getter
@Setter
public class SpecialSurvey extends BaseEntity implements BaseSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @ManyToMany(mappedBy = "specialSurveys") // ← inverse side
    private List<SurveySet> surveySets = new ArrayList<>();

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @Column(nullable = false)
    private String category;


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
