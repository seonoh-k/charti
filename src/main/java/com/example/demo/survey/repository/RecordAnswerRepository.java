package com.example.demo.survey.repository;

import com.example.demo.survey.dto.RecordDateSummaryDto;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 기록 문진 답변(RecordAnswer) 관련 JPA 인터페이스.
 * - 일반 회원/관리자의 문진 이력 조회, 날짜 기반 정리, 중복 확인 등 다양한 조회 쿼리 포함
 */
public interface RecordAnswerRepository extends JpaRepository<RecordAnswer, Long> {

    /**
     * [회원용] 작성자 기준으로 삭제되지 않은 모든 답변 조회
     */
    List<RecordAnswer> findByWriterAndDeletedFalse(Member writer);

    /**
     * [회원용] 작성자 + 자녀 기준으로 삭제되지 않은 답변 조회
     */
    List<RecordAnswer> findByWriterAndChildAndDeletedFalse(Member writer, Child child);

    /**
     * [관리자용] 특정 자녀의 '문진 날짜 목록'을 중복 없이 페이징하여 반환
     * - 날짜는 createdAt의 DATE 부분 기준
     * - RecordDateSummaryDto에 날짜 + 자녀ID를 매핑하여 반환
     */
    @Query(value = "SELECT new com.example.demo.survey.dto.RecordDateSummaryDto(" +
            "DATE(ra.createdAt), ra.child.id) " +
            "FROM RecordAnswer ra " +
            "WHERE ra.child = :child AND ra.deleted = false " +
            "GROUP BY DATE(ra.createdAt), ra.child.id " +
            "ORDER BY DATE(ra.createdAt) DESC",
            countQuery = "SELECT COUNT(DISTINCT DATE(ra.createdAt)) " +
                    "FROM RecordAnswer ra " +
                    "WHERE ra.child = :child AND ra.deleted = false")
    Page<RecordDateSummaryDto> findDistinctRecordDatesByChild(@Param("child") Child child, Pageable pageable);

    /**
     * [관리자용] 특정 자녀의 특정 날짜에 작성한 문진 답변 리스트 조회
     * - 날짜는 LocalDateTime 범위(start ~ end)로 조회
     */
    List<RecordAnswer> findByChildAndCreatedAtBetween(Child child, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * [관리자용] 특정 자녀가 문진 응답을 작성한 '고유 날짜' 수를 계산 (페이징 totalCount)
     */
    @Query("SELECT COUNT(DISTINCT FUNCTION('DATE', r.createdAt)) " +
            "FROM RecordAnswer r " +
            "WHERE r.child = :child AND r.deleted = false")
    long countDistinctRecordDatesByChild(@Param("child") Child child);

    /**
     * [중복 체크] 특정 자녀가 오늘 날짜 기준으로 이미 문진을 작성했는지 여부 확인
     */
    long countByChildAndCreatedAtBetween(Child child, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * [관리자용] 자녀ID + 날짜 기준으로 문진 답변 전체 조회 (수정/삭제용)
     */
    List<RecordAnswer> findByChild_IdAndCreatedAtBetween(Long childId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * ✨ [회원용/페이징] 특정 자녀의 모든 기록 문진 답변을 페이징하여 조회합니다.
     * createdAt 필드를 기준으로 내림차순 정렬됩니다.
     */
    Page<RecordAnswer> findByChildAndDeletedFalse(Child child, Pageable pageable);

    /**
     * 특정 childId 가 주어진 기간(start~end)에 답변을 남겼는지 여부 확인
     */
    boolean existsByChildIdAndCreatedAtBetween(Long id, LocalDateTime start, LocalDateTime end);
}
