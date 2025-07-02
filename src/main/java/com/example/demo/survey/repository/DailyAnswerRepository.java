package com.example.demo.survey.repository;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.DailyAnswer;
import com.example.demo.users.dto.ChildRecordCountDto;
import com.example.demo.users.entity.Child;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DailyAnswerRepository extends JpaRepository<DailyAnswer, Long> {
    /**
     * childId, category, ageGroup 조건으로 필터링.
     * 파라미터가 null 이면 해당 조건은 무시합니다.
     * 페이징은 Pageable 에 의해 처리됩니다.
     */
    @Query("""
      select da
      from DailyAnswer da
      join da.survey s
      where (:childId   is null or da.child.id     = :childId)
        and (:category  is null or da.category      = :category)
        and (:ageGroup  is null or s.ageGroup       = :ageGroup)
      order by da.createdAt desc
    """)
    Page<DailyAnswer> findByFilters(
            Long childId,
            SurveyCategory category,
            AgeGroup ageGroup,
            Pageable pageable
    );

    List<DailyAnswer> findByChildIdAndDeletedFalseOrderByCreatedAtDesc(Long childId);

    /**
     * 특정 childId 가 주어진 기간(start~end)에 답변을 남겼는지 여부 확인
     */
    boolean existsByChildIdAndCreatedAtBetween(Long childId,
                                               LocalDateTime start,
                                               LocalDateTime end);

    Page<DailyAnswer> findAllByChildIdAndDeletedFalse(Long childId, Pageable pageable);

    boolean existsByChildAndCreatedAtBetween(Child child, LocalDateTime from, LocalDateTime to);

    /**
     * 여러 자녀에 대한 각각의 고유 문진 기록일 카운트를 한 번의 쿼리로 조회합니다. (N+1 문제 해결용)
     * @param children 조회할 자녀 엔티티 목록
     * @return 각 자녀의 ID와 기록일 카운트를 담은 ChildRecordCountDto 목록
     */
    @Query("SELECT new com.example.demo.users.dto.ChildRecordCountDto(r.child.id, COUNT(DISTINCT FUNCTION('DATE', r.createdAt))) " +
            "FROM DailyAnswer r " +
            "WHERE r.child IN :children AND r.deleted = false " +
            "GROUP BY r.child.id")
    List<ChildRecordCountDto> countDistinctSpecialDatesByChildren(@Param("children") List<Child> children);

    /**
     * 특정 자녀의 '기록 날짜' 목록을 중복 없이 페이징하여 조회합니다.
     * DTO 생성을 서비스 계층에 위임하기 위해, 순수 데이터(Object[])를 반환합니다.
     * @param child 조회할 자녀 엔티티
     * @param pageable 페이징 정보
     * @return Object 배열(0: 날짜, 1: 자녀 ID)을 담은 Page 객체
     */
    @Query(value = "SELECT FUNCTION('DATE', ra.createdAt), ra.child.id " +
            "FROM DailyAnswer ra " +
            "WHERE ra.child = :child AND ra.deleted = false " +
            "GROUP BY FUNCTION('DATE', ra.createdAt), ra.child.id " +
            "ORDER BY FUNCTION('DATE', ra.createdAt) DESC",
            countQuery = "SELECT COUNT(DISTINCT FUNCTION('DATE', ra.createdAt)) " +
                    "FROM DailyAnswer ra " +
                    "WHERE ra.child = :child AND ra.deleted = false")
    Page<Object[]> findDistinctSpecialDatesByChild(@Param("child") Child child, Pageable pageable);

    /**
     * 특정 자녀의 특정 날짜에 작성된 모든 문진 답변 목록을 조회합니다.
     * @param child 자녀 엔티티
     * @param startDateTime 조회 시작 일시
     * @param endDateTime 조회 종료 일시
     * @return RecordAnswer 엔티티 목록
     */
    List<DailyAnswer> findByChildAndCreatedAtBetween(Child child, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 특정 자녀 ID와 특정 날짜 범위를 기준으로 모든 문진 답변을 조회합니다. (관리자 수정/삭제용)
     * @param childId 자녀 ID
     * @param startDateTime 조회 시작 일시
     * @param endDateTime 조회 종료 일시
     * @return RecordAnswer 엔티티 목록
     */
    List<DailyAnswer> findByChild_IdAndCreatedAtBetween(Long childId, LocalDateTime startDateTime, LocalDateTime endDateTime);

}
