package com.example.demo.repository;

import com.example.demo.entity.PointHistory;
import com.example.demo.enums.PointType;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 포인트 이력 저장소
 * - 회원의 포인트 변경 이력을 관리하고 중복 지급 여부를 검증합니다.
 */
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    /**
     * 특정 회원의 포인트 이력을 생성일 기준 내림차순(최신순)으로 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 포인트 이력 리스트 (최신순 정렬)
     */
    List<PointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 특정 회원의 포인트 이력을 생성일 기준 오름차순(오래된 순)으로 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 포인트 이력 리스트 (오래된 순 정렬)
     */
    List<PointHistory> findByMemberIdOrderByCreatedAtAsc(Long memberId);

    /**
     * 지정한 회원(Member)이 특정 자녀(Child)에 대해 특정 날짜(LocalDate)에
     * 같은 유형(PointType)의 포인트를 이미 지급했는지 확인합니다.
     * <p>
     * 기록 문진(RECORD_SURVEY) 포인트는 하루에 자녀별 1회만 지급 가능하므로,
     * 이 메서드는 중복 지급 방지를 위해 사용됩니다.
     *
     * @param member    포인트 지급 회원
     * @param pointType 포인트 유형 (예: RECORD_SURVEY)
     * @param child     포인트 지급 대상 자녀
     * @param pointDate 포인트 지급 날짜
     * @return true: 이미 지급함, false: 아직 지급 안됨
     */
    boolean existsByMemberAndPointTypeAndChildAndPointDate(
            Member member,
            PointType pointType,
            Child child,
            LocalDate pointDate
    );
}
