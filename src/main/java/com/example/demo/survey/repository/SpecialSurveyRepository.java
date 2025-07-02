// com.example.demo.survey.repository.SpecialSurveyRepository.java
package com.example.demo.survey.repository;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.SpecialSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpecialSurveyRepository extends JpaRepository<SpecialSurvey, Long> {

    @Query("""
        select ss
          from SpecialSurvey ss
          join ss.surveySets sset
         where sset.setId = :setId
        """)
    List<SpecialSurvey> findBySurveySetId(@Param("setId") Long setId);

    List<SpecialSurvey> findByAgeGroupAndDeletedFalse(AgeGroup ag);
    List<SpecialSurvey> findByCategoryAndDeletedFalse(SurveyCategory sc);

    // 신규 메소드 추가
    List<SpecialSurvey> findByAgeGroupAndCategoryAndDeletedFalse(AgeGroup ageGroup, SurveyCategory category);

    @Query("""
        select distinct d.category 
          from GroupSurvey d 
         where d.deleted = false
        """)
    List<SurveyCategory> findDistinctCategories();

    Page<SpecialSurvey> findAllByDeletedFalse(Pageable pageable);

    Page<SpecialSurvey> findByAgeGroupAndDeletedFalse(AgeGroup ag, Pageable pageable);

    Page<SpecialSurvey> findAllByCategoryAndDeletedFalse(SurveyCategory category, Pageable pageable);

    Page<SpecialSurvey> findByAgeGroupAndCategoryAndDeletedFalse(AgeGroup ageGroup, SurveyCategory category, Pageable pageable);
}