package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_survey")
@Getter
@Setter
public class DailySurvey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_item_id", nullable = false)
    private SurveySetItem surveySetItem;
}
