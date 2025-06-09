package com.example.demo.repository;

import com.example.demo.entity.RecordSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordSurveyRepository extends JpaRepository<RecordSurvey, Long> {
    List<RecordSurvey> findByDeletedFalse();
}
