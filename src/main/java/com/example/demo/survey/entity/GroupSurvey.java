package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Manager;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "group_survey")
@Getter
@Setter
public class GroupSurvey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Manager manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private SurveySet surveySet;

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;

    @Column(name = "target_group", nullable = false)
    private String targetGroup;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @Column(name = "kindergarten_group", nullable = false)
    private String kindergartenGroup;

    @Column(nullable = false)
    private String answer1;

    @Column(nullable = false)
    private String answer2;

    @Column(nullable = false)
    private String answer3;

    @Column
    private String answer4;

    @Column
    private String answer5;

    @Column
    private String title;
}
