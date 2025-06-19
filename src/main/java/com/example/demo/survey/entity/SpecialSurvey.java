package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.converter.SurveyCategoryConverter;
import com.example.demo.converter.AgeGroupConverter;
import com.example.demo.enums.TargetGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class SpecialSurvey extends BaseEntity implements BaseSurvey{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private SurveyCategory category;

    @ManyToMany(mappedBy = "specialSurveys")
    @JsonIgnore
    private List<SurveySet> surveySets = new ArrayList<>();

    @Column(nullable = false)
    private int weight;

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


    @Override
    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    @Override
    public SurveyCategory getCategory() {
        return category;
    }


    @Override
    public Long getId() {
        return id;
    }



}
