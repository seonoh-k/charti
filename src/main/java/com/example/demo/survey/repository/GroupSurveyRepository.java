package com.example.demo.survey.repository;

import com.example.demo.survey.entity.GroupSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupSurveyRepository extends JpaRepository<GroupSurvey, Long> {
    List<GroupSurvey> findByAgeGroupAndDeletedFalse(String ageGroup);
    List<GroupSurvey> findByTargetGroupAndDeletedFalse(String targetGroup);
}