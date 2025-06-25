package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.enums.AgeGroup;
import com.example.demo.matching.entity.Matching;
import com.example.demo.users.entity.Child;
import com.example.demo.enums.SurveyCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="special_answer")
@Getter @Setter
public class SpecialAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "special_answer_id")
    private Long id;

    // --- Matching 과 N:1 (여러 Answer → 하나 Matching) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    //  특별 문진 세트 (special_survey_set) 과 N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_set_id", nullable = false)
    private SurveySet surveySet;

    // 자녀 (child) 과 N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    // 연령대
    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    // SpecialSurvey.category 와 같은 값
    @Column(nullable = false)
    private SurveyCategory category;

    @Column(nullable = false)
    private String question;

    // 실제 사용자가 선택한 답변 텍스트
    @Column(nullable = false)
    private String answer;


}