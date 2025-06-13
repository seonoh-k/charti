package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_survey")
@Getter
@Setter
public class GroupSurvey extends BaseEntity implements BaseSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(nullable = false)
    private String category;


//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name = "survey_set_id", nullable = false)
//    private List<SurveySet> surveySets;


    @ManyToMany(mappedBy = "groupSurveys")  // ← 이제 inverse side
    private List<SurveySet> surveySets = new ArrayList<>();

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;

    @Column(name = "target_group", nullable = false)
    private String targetGroup;

    @Column(nullable = false)
    private int weight;

    // 선택지 텍스트 저장 (필요 시 프론트에서 사용)
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

    // 실제 선택된 답변 (텍스트)
    // answer 에 들어가는 객관식 답변항목이 달라질 수 있기 때문에 추가
    @Column(name = "selected_answer", nullable = false)
    private String selectedAnswer;

    // 계산된 점수
    @Column(name = "calculated_score", nullable = false)
    private int calculatedScore;
}
