package com.example.demo.matching.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.enums.MatchingStatus;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.users.entity.Child;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "matching")
@Getter @Setter
public class Matching extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long id;

    // (특별 문진 답변) SpecialAnswer 와 N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private SpecialAnswer specialAnswer;

    // 자녀(Child) 와 N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    // SurveyCategory
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurveyCategory category;

    // 상담 제목
    @Column(nullable = false)
    private String title;

    // 상담 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 상담 상태 (기본: REQUESTED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingStatus status = MatchingStatus.REQUESTED;
}
