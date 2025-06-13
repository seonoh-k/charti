package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_set")
@Getter
@Setter
public class SurveySet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long setId;

    @Column(nullable = false)
    private String setTitle;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;  // 예: "0~1세"

    @Column(nullable = false)
    private String type; // 예: "GROUP" / "SPECIAL"

    @Column(nullable = false)
    private String category; // 예: "사회성/정서", "생활습관"


    @ManyToMany  // ← owning side
    @JoinTable(
            name = "group_survey_set",
            joinColumns =  @JoinColumn(name = "survey_set_id"),
            inverseJoinColumns = @JoinColumn(name = "group_survey_id")
    )
    private List<GroupSurvey> groupSurveys = new ArrayList<>();

    @ManyToMany  // ← owning side
    @JoinTable(
            name = "special_survey_set",
            joinColumns =  @JoinColumn(name = "survey_set_id"),
            inverseJoinColumns = @JoinColumn(name = "special_survey_id")
    )
    private List<SpecialSurvey> specialSurveys = new ArrayList<>();
}
