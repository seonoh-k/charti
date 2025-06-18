package com.example.demo.survey.repository;

import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.entity.RecordSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordSurveyRepository extends JpaRepository<RecordSurvey, Long> {

    // 연령대별 문진 (소프트 삭제 제외)
    List<RecordSurvey> findByAgeGroupAndDeletedFalse(AgeGroup ageGroup);

    // 연령대별 문진 + 페이징 지원
    Page<RecordSurvey> findByAgeGroupAndDeletedFalse(AgeGroup ageGroup, Pageable pageable);

    // 전체 문진 목록 (삭제 제외)
    List<RecordSurvey> findAllByDeletedFalse();

    // 전체 문진 목록 + 페이징
    Page<RecordSurvey> findAllByDeletedFalse(Pageable pageable);
}