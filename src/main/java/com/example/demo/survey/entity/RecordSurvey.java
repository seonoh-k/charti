package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "record_survey")
@Getter @Setter
public class RecordSurvey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long surveyId;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    public void markAsDeleted() {
        this.setDeleted(true);
        this.setDeletedAt(LocalDateTime.now());
    }


}
