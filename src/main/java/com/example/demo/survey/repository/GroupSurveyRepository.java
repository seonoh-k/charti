package com.example.demo.survey.repository;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.GroupSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface GroupSurveyRepository extends JpaRepository<GroupSurvey, Long> {

    @Query("""
        select distinct d.category 
          from GroupSurvey d 
         where d.deleted = false
        """)
    List<SurveyCategory> findDistinctCategories();

    @Query("""
        select gs
          from GroupSurvey gs
          join gs.surveySets ss
         where ss.setId = :setId
        """)
    List<GroupSurvey> findBySurveySetId(@Param("setId") Long setId);

    List<GroupSurvey> findByAgeGroupAndDeletedFalse(AgeGroup ag);

    List<GroupSurvey> findByCategoryAndDeletedFalse(SurveyCategory sc);


    @Query("select gs from GroupSurvey gs where gs.targetGroup like concat(:groupPrefix, '%') and gs.deleted = false")
    List<GroupSurvey> findByTargetGroupPrefix(@Param("groupPrefix") String groupPrefix);

    Page<GroupSurvey> findAllByDeletedFalse(Pageable pageable);

    Page<GroupSurvey> findByAgeGroupAndDeletedFalse(AgeGroup ag, Pageable pageable);

    Page<GroupSurvey> findAllByCategoryAndDeletedFalse(SurveyCategory category, Pageable pageable);

    Page<GroupSurvey> findByAgeGroupAndCategoryAndDeletedFalse(AgeGroup ageGroup, SurveyCategory category, Pageable pageable);
}
