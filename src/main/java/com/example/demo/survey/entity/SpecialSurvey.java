package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name = "special_survey")
@Getter
@Setter
public class SpecialSurvey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private SurveySet surveySet;

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;

    @Column(nullable = false)
    private String answer1;

    @Column(nullable = false)
    private String answer2;

    @Column(nullable = false)
    private String answer3;

    @Column(nullable = true)
    private String answer4;

    @Column(nullable = true)
    private String answer5;

    @Column(name = "title", nullable = true)
    private String title;

}