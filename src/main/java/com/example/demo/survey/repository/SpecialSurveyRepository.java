package com.example.demo.survey.repository;

import com.example.demo.survey.entity.SpecialSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialSurveyRepository extends JpaRepository<SpecialSurvey, Long> {
    List<SpecialSurvey> findByChildId(Long childId);
}
