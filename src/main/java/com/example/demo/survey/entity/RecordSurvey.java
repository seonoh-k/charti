package com.example.demo.survey.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.enums.AgeGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "연령대를 선택해주세요.")
    private AgeGroup ageGroup;

    @Column(nullable = false)
    @NotBlank(message = "질문을 입력해주세요.")
    private String question;

    @Column(nullable = false)
    private String answer;

    public void markAsDeleted() {
        this.setDeleted(true);
        this.setDeletedAt(LocalDateTime.now());
    }
}
