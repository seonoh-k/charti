package com.example.demo.survey.repository;

import com.example.demo.survey.entity.RecordSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordSurveyRepository extends JpaRepository<RecordSurvey, Long> {
    List<RecordSurvey> findByDeletedFalse();
    List<RecordSurvey> findByAgeGroupAndDeletedFalse(String ageGroup); // 연령대 필터용
    List<RecordSurvey> findAllByDeletedFalse(); // 전체 조회

    Page<RecordSurvey> findByDeletedFalse(Pageable pageable);
    Page<RecordSurvey> findByAgeGroupAndDeletedFalse(String ageGroup, Pageable pageable);

}
