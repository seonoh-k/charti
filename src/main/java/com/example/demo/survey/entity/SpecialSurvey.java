package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "special_survey")
public class SpecialSurvey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK로 설정: 위험군으로 분류된 아이
    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;

    @ManyToOne
    @JoinColumn(name = "set_item_id", nullable = false)
    private SurveySetItem surveySetItem;
}
