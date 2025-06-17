package com.example.demo.survey.repository;


import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.DailySurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailySurveyRepository extends JpaRepository<DailySurvey, Long> {

    // 연령대와 카테고리로 문진 찾기
    List<DailySurvey> findByAgeGroupAndCategoryAndDeletedFalse(AgeGroup ageGroup, SurveyCategory category);

    // 연령대별 전체 문진 (소프트삭제 제외)
    List<DailySurvey> findByAgeGroupAndDeletedFalse(AgeGroup ageGroup);

    // 관리 페이지에서 사용할, 삭제되지 않은 설문카테고리 목록 (중복 제거)
    @Query("select distinct d.category from DailySurvey d where d.deleted = false")
    List<SurveyCategory> findDistinctCategories();

        // 전체(삭제되지 않은) 문진 전체 조회
        List<DailySurvey> findAllByDeletedFalse();

        // 카테고리 필터 조회
        List<DailySurvey> findAllByCategoryAndDeletedFalse(SurveyCategory category);
}
