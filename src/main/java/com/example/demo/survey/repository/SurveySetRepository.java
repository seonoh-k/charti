package com.example.demo.survey.repository;

import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.entity.SurveySet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    /**
     * [담당자 대상] 특정 TargetGroup(예: 유치원)과 연결된 그룹 문진 세트를 조회
     *
     * @param targetGroup 담당자의 그룹 종류 (KINDERGARTEN, DAYCARE 등)
     * @return 해당 그룹 대상 문진 세트 목록
     */
    @Query("""
    SELECT DISTINCT s FROM SurveySet s
    JOIN s.groupSurveys gs
    WHERE s.type = 'GROUP' AND gs.targetGroup = :targetGroup
""")
    List<SurveySet> findAllByTargetGroupForManager(@Param("targetGroup") TargetGroup targetGroup);

    /**
     *  타입이 'GROUP'인 모든 문진 세트를 조회합니다.
     * 담당자의 소속 그룹과 관계 없이, 모든 그룹 문진 세트를 가져옵니다.
     */
    List<SurveySet> findByType(String type);

}