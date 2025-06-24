package com.example.demo.matching.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.enums.MatchingStatus;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Expert;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matching")
@Getter @Setter
public class Matching extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long id;

    // (특별 문진 답변) SpecialAnswer 와 N:1
    @OneToMany(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecialAnswer> answers = new ArrayList<>();

    // 자녀(Child)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    // 전문가(Expert)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private Expert expert;

    // SurveyCategory
    @Column(nullable = false)
    private SurveyCategory category;

    // 상담 제목
    @Column(nullable = false)
    private String title;

    // 상담 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 상담 상태 (기본: 신청완료)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingStatus status = MatchingStatus.REQUESTED;

    // 헬퍼 메서드 (양방향 연관관계 설정)
    public void addAnswer(SpecialAnswer answer) {
        answers.add(answer);
        answer.setMatching(this);
    }
}
