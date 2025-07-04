package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "group_survey")
@Getter
@Setter
public class GroupSurvey extends BaseEntity implements BaseSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(nullable = false)
    private SurveyCategory category;

    @ManyToMany(mappedBy = "groupSurveys")
    @JsonIgnore
    private List<SurveySet> surveySets = new ArrayList<>();

    @Column(name = "target_group", nullable = false)
    private TargetGroup targetGroup;

//    @Column(nullable = false)
//    private int weight;

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



    @Override
    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    @Override
    public SurveyCategory getCategory() {
        return category;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Optional<TargetGroup> getTargetGroup() {
        return Optional.of(targetGroup);
    }

    public TargetGroup getRawTargetGroup() {
        return this.targetGroup;
    }
}
