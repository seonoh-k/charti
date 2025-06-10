package com.example.demo.survey.repository;

import com.example.demo.survey.entity.RecordSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordSurveyRepository extends JpaRepository<RecordSurvey, Long> {
    List<RecordSurvey> findByDeletedFalse();
}
