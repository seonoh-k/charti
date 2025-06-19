// com.example.demo.survey.entity.GroupAnswer.java
package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.enums.AgeGroup;
import com.example.demo.users.entity.Child;
import com.example.demo.enums.TargetGroup;
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

    //  특별 문진 항목 (special_survey) 과 N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SpecialSurvey survey;

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

    // 가중치(위험도 도출 시 사용)
    @Column(nullable = false)
    private Integer weight;
}
