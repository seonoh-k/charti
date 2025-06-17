package com.example.demo.survey.repository;

import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.entity.RecordSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordSurveyRepository extends JpaRepository<RecordSurvey, Long> {

    // 삭제되지 않은 전체
    List<RecordSurvey> findByDeletedFalse();

    // 삭제되지 않고 연령대가 일치하는 목록
    List<RecordSurvey> findByAgeGroupAndDeletedFalse(AgeGroup ageGroup);

    // 삭제되지 않은 전체 (중복 제거 가능)
    List<RecordSurvey> findAllByDeletedFalse();

    // 삭제되지 않은 페이지
    Page<RecordSurvey> findByDeletedFalse(Pageable pageable);

    // 삭제되지 않고 연령대가 일치하는 페이지
    Page<RecordSurvey> findByAgeGroupAndDeletedFalse(AgeGroup ageGroup, Pageable pageable);
}
