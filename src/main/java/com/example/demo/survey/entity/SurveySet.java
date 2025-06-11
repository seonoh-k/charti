package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    // 설문 세트 이름 (예: "0~12개월_생활습관")
    @Column(name = "title")
    private String title;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private int weight;

}
