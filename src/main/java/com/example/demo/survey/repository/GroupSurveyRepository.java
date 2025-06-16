package com.example.demo.survey.repository;

import com.example.demo.survey.entity.GroupSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

public interface GroupSurveyRepository extends JpaRepository<GroupSurvey, Long> {
    List<GroupSurvey> findByAgeGroupAndDeletedFalse(String ageGroup);
    List<GroupSurvey> findByTargetGroupAndDeletedFalse(String targetGroup);

    List<GroupSurvey> findByCategoryAndDeletedFalse(String category);

    @Query("select distinct d.category from GroupSurvey d where d.deleted = false")
    List<String> findDistinctCategories();

    @Query("""
        select gs 
          from GroupSurvey gs 
          join gs.surveySets ss 
         where ss.setId = :setId
        """)
    List<GroupSurvey> findBySurveySetId(@Param("setId") Long setId);

}