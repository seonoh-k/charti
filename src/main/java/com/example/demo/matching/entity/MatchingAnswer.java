package com.example.demo.matching.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Expert;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "matching_ans")
@Getter @Setter
public class MatchingAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_ans_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    // 자녀 정보는 matching.child 에서 가져올 수 있으므로 FK만 저장
    @Column(name="child_id", nullable = false)
    private Long childId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
