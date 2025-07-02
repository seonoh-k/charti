package com.example.demo.survey.entity;

import com.example.demo.converter.TargetGroupConverter;
import com.example.demo.entity.BaseEntity;
import com.example.demo.enums.AgeGroup;
import com.example.demo.converter.AgeGroupConverter;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.converter.SurveyCategoryConverter;
import com.example.demo.enums.TargetGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_set")
@Getter
@Setter
@SQLDelete(sql = """
    UPDATE survey_set
       SET deleted = true, deleted_at = now()
     WHERE set_id = ?
""")
@Where(clause = "deleted = false")
public class SurveySet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long setId;

    @Column(nullable = false)
    private String setTitle;

    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    @Column(nullable = false)
    private SurveyCategory category;

    @Column(nullable = false)
    private String type; // 예: "GROUP" / "SPECIAL"

//    @Enumerated(EnumType.STRING)
//    @Column(name = "target_group", nullable = true)
//    private TargetGroup targetGroup;
    @Convert(converter = TargetGroupConverter.class)
    @Column(name = "target_group", nullable = true)
    private TargetGroup targetGroup;

    @ManyToMany
    @JoinTable(
            name = "group_survey_set",
            joinColumns =  @JoinColumn(name = "survey_set_id"),
            inverseJoinColumns = @JoinColumn(name = "group_survey_id")
    )
    private List<GroupSurvey> groupSurveys = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "special_survey_set",
            joinColumns =  @JoinColumn(name = "survey_set_id"),
            inverseJoinColumns = @JoinColumn(name = "special_survey_id")
    )
    private List<SpecialSurvey> specialSurveys = new ArrayList<>();
}