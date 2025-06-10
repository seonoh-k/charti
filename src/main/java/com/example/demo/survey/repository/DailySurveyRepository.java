package com.example.demo.survey.repository;


import com.example.demo.survey.entity.DailySurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailySurveyRepository extends JpaRepository<DailySurvey, Long> {

    // 연령대와 카테고리로 문진 찾기
    List<DailySurvey> findByAgeGroupAndCategoryAndDeletedFalse(String ageGroup, String category);

    // 연령대별 전체 문진 (소프트삭제 제외)
    List<DailySurvey> findByAgeGroupAndDeletedFalse(String ageGroup);
}
