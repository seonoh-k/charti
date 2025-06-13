package com.example.demo.survey.repository;

import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.entity.GroupSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupSurveyRepository extends JpaRepository<GroupSurvey, Long> {
    List<GroupSurvey> findByAgeGroupAndCategoryAndDeletedFalse(String ageGroup, String category);
    List<GroupSurvey> findByTargetGroupAndCategoryAndDeletedFalse(String targetGroup,String category);

    List<GroupSurvey> findByAgeGroupAndDeletedFalse(String ageGroup);

    List<GroupSurvey>findByTargetGroupAndDeletedFalse(String targetGroup);

    @Query("select distinct d.category from GroupSurvey d where d.deleted = false")
    List<String> findDistinctCategories();
}