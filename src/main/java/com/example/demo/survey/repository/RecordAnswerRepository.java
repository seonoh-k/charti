package com.example.demo.survey.repository;

import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface RecordAnswerRepository extends JpaRepository<RecordAnswer, Long> {

    // ✅ 본인이 작성한 모든 답변 조회 (삭제되지 않은 것만)
    List<RecordAnswer> findByWriterAndDeletedFalse(Member writer);

    // ✅ 본인이 특정 자녀에 대해 작성한 답변 조회 (삭제되지 않은 것만)
    List<RecordAnswer> findByWriterAndChildAndDeletedFalse(Member writer, Child child);

    // ✅ [관리자용] 자녀 기준으로 '응답한 날짜 목록' 조회 (중복 제거, 최신순, 페이징)
    // 반환 타입을 Page<LocalDate> -> Page<Date> 로 변경
    @Query("SELECT DISTINCT DATE(r.createdAt) " +
            "FROM RecordAnswer r " +
            "WHERE r.child = :child AND r.deleted = false " +
            "ORDER BY DATE(r.createdAt) DESC")
    Page<java.util.Date> findDistinctAnswerDatesByChild(@Param("child") Child child, Pageable pageable);

    // ✅ [관리자용] 특정 자녀의 특정 날짜에 작성한 모든 답변 조회 (질문/답변 리스트 출력용)
    @Query("SELECT r FROM RecordAnswer r " +
            "WHERE r.child = :child AND r.deleted = false " +
            "AND DATE(r.createdAt) = :date " +
            "ORDER BY r.createdAt ASC")
    List<RecordAnswer> findByChildAndDate(@Param("child") Child child, @Param("date") LocalDate date);

    // ✅ [관리자용] 특정 자녀가 응답한 날짜의 총 개수 (페이징 처리용 totalCount)
    @Query("SELECT COUNT(DISTINCT DATE(r.createdAt)) " +
            "FROM RecordAnswer r " +
            "WHERE r.child = :child AND r.deleted = false")
    long countDistinctAnswerDatesByChild(@Param("child") Child child);
}
