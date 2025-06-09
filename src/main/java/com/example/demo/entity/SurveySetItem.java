package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "survey_set_item")
@Getter
@Setter
public class SurveySetItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_set_id", nullable = false)
    private SurveySet surveySet;


    // 설문이 표시되는 순서
    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_required")
    private Boolean isRequired = true;
}
