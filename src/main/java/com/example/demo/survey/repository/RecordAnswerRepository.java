package com.example.demo.survey.repository;

import com.example.demo.users.dto.ChildRecordCountDto;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 기록 문진 답변(RecordAnswer) 관련 데이터 접근을 위한 JPA Repository 인터페이스입니다.
 */
public interface RecordAnswerRepository extends JpaRepository<RecordAnswer, Long> {

    /**
     * 특정 작성자가 작성한 모든 기록 문진 답변 목록을 조회합니다. (삭제된 답변 제외)
     * @param writer 작성자(회원) 엔티티
     * @return RecordAnswer 엔티티 목록
     */
    List<RecordAnswer> findByWriterAndDeletedFalse(Member writer);

    /**
     * 특정 작성자가 특정 자녀에 대해 작성한 모든 기록 문진 답변 목록을 조회합니다. (삭제된 답변 제외)
     * @param writer 작성자(회원) 엔티티
     * @param child 자녀 엔티티
     * @return RecordAnswer 엔티티 목록
     */
    List<RecordAnswer> findByWriterAndChildAndDeletedFalse(Member writer, Child child);

    /**
     * 특정 작성자가 특정 자녀에 대해 작성한 기록 문진 답변 목록을 페이징하여 조회합니다. (삭제된 답변 제외)
     * @param writer 작성자(회원) 엔티티
     * @param child 자녀 엔티티
     * @param pageable 페이징 정보
     * @return RecordAnswer 엔티티를 담은 Page 객체
     */
    Page<RecordAnswer> findByWriterAndChildAndDeletedFalseOrderByCreatedAtDesc(Member writer, Child child, Pageable pageable);

    /**
     * 특정 자녀의 '기록 날짜' 목록을 중복 없이 페이징하여 조회합니다.
     * DTO 생성을 서비스 계층에 위임하기 위해, 순수 데이터(Object[])를 반환합니다.
     * @param child 조회할 자녀 엔티티
     * @param pageable 페이징 정보
     * @return Object 배열(0: 날짜, 1: 자녀 ID)을 담은 Page 객체
     */
    @Query(value = "SELECT FUNCTION('DATE', ra.createdAt), ra.child.id " +
            "FROM RecordAnswer ra " +
            "WHERE ra.child = :child AND ra.deleted = false " +
            "GROUP BY FUNCTION('DATE', ra.createdAt), ra.child.id " +
            "ORDER BY FUNCTION('DATE', ra.createdAt) DESC",
            countQuery = "SELECT COUNT(DISTINCT FUNCTION('DATE', ra.createdAt)) " +
                    "FROM RecordAnswer ra " +
                    "WHERE ra.child = :child AND ra.deleted = false")
    Page<Object[]> findDistinctRecordDatesByChild(@Param("child") Child child, Pageable pageable);

    /**
     * 특정 자녀의 특정 날짜에 작성된 모든 문진 답변 목록을 조회합니다.
     * @param child 자녀 엔티티
     * @param startDateTime 조회 시작 일시
     * @param endDateTime 조회 종료 일시
     * @return RecordAnswer 엔티티 목록
     */
    List<RecordAnswer> findByChildAndCreatedAtBetween(Child child, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 특정 자녀가 문진에 응답한 고유한 날짜의 총 수를 계산합니다.
     * @param child 자녀 엔티티
     * @return 고유 날짜 수
     */
    @Query("SELECT COUNT(DISTINCT FUNCTION('DATE', r.createdAt)) " +
            "FROM RecordAnswer r " +
            "WHERE r.child = :child AND r.deleted = false")
    long countDistinctRecordDatesByChild(@Param("child") Child child);

    /**
     * 특정 자녀가 특정 기간 내에 문진을 작성했는지 여부를 확인합니다. (중복 작성 체크용)
     * @param child 자녀 엔티티
     * @param startDateTime 조회 시작 일시
     * @param endDateTime 조회 종료 일시
     * @return 존재 여부 (true/false)
     */
    boolean existsByChildAndCreatedAtBetween(Child child, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 특정 자녀 ID와 특정 날짜 범위를 기준으로 모든 문진 답변을 조회합니다. (관리자 수정/삭제용)
     * @param childId 자녀 ID
     * @param startDateTime 조회 시작 일시
     * @param endDateTime 조회 종료 일시
     * @return RecordAnswer 엔티티 목록
     */
    List<RecordAnswer> findByChild_IdAndCreatedAtBetween(Long childId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 특정 자녀의 모든 기록 문진 답변을 페이징하여 조회합니다. (작성자 무관, createdAt 내림차순 정렬)
     * @param child 자녀 엔티티
     * @param pageable 페이징 정보
     * @return RecordAnswer 엔티티를 담은 Page 객체
     */
    Page<RecordAnswer> findByChildAndDeletedFalse(Child child, Pageable pageable);

    /**
     * 여러 자녀에 대한 각각의 고유 문진 기록일 카운트를 한 번의 쿼리로 조회합니다. (N+1 문제 해결용)
     * @param children 조회할 자녀 엔티티 목록
     * @return 각 자녀의 ID와 기록일 카운트를 담은 ChildRecordCountDto 목록
     */
    @Query("SELECT new com.example.demo.users.dto.ChildRecordCountDto(r.child.id, COUNT(DISTINCT FUNCTION('DATE', r.createdAt))) " +
            "FROM RecordAnswer r " +
            "WHERE r.child IN :children AND r.deleted = false " +
            "GROUP BY r.child.id")
    List<ChildRecordCountDto> countDistinctRecordDatesByChildren(@Param("children") List<Child> children);
}