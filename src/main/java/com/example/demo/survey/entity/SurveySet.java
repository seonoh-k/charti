package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name = "survey_set")
@Getter
@Setter
public class SurveySet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long setId;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private int weight;

    @Column(nullable = false)
    private String answer1;

    @Column(nullable = false)
    private String answer2;

    @Column(nullable = false)
    private String answer3;

    @Column(nullable = false)
    private String answer4;

    @Column(nullable = false)
    private String answer5;

    @OneToMany(mappedBy = "surveySet", cascade = CascadeType.ALL)
    private List<SurveySetItem> items;

}
