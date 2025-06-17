package com.example.demo.survey.repository;

import com.example.demo.survey.entity.SurveySet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SurveySetRepository
        extends JpaRepository<SurveySet, Long>,
        JpaSpecificationExecutor<SurveySet> {

    // 조회 시점에 항상 이 세트에 속한 문진들을 같이 가져오도록 설정(페치 조인)
    // GROUP 세트일 때 groupSurveys만 함께 가져오기
    @EntityGraph(attributePaths = "groupSurveys")
    Optional<SurveySet> findGroupWithGroupSurveysBySetId(Long setId);

    // SPECIAL 세트일 때 specialSurveys만 함께 가져오기
    @EntityGraph(attributePaths = "specialSurveys")
    Optional<SurveySet> findSpecialWithSpecialSurveysBySetId(Long setId);
}