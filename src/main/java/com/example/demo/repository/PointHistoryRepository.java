package com.example.demo.repository;

import com.example.demo.entity.PointHistory;
import com.example.demo.enums.PointType;
import com.example.demo.users.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    /**
     * [회원 포인트 이력 조회 - 최신순]
     * - 특정 회원의 포인트 이력을 생성일 기준 내림차순으로 조회
     */
    List<PointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * [회원 포인트 이력 조회 - 오래된 순]
     * - 특정 회원의 포인트 이력을 생성일 기준 오름차순으로 조회
     */
    List<PointHistory> findByMemberIdOrderByCreatedAtAsc(Long memberId);

    /**
     * [기록문진 포인트 중복 지급 여부 확인]
     *
     * 특정 보호자(member)가 특정 자녀(childName)에게
     * 특정 유형(pointType)의 포인트를 같은 날짜(pointDate)에 이미 지급했는지를 검사합니다.
     *
     * - 기록문진 포인트의 경우: 하루에 한 번, 자녀별 1회만 지급해야 하므로 중복 방지에 사용됩니다.
     * - true 반환 시: 이미 지급된 상태이므로 추가 지급 불가
     * - false 반환 시: 아직 지급되지 않았으므로 지급 가능
     *
     * @param member     포인트를 받은 보호자
     * @param pointType  포인트 유형 (예: RECORD_SURVEY)
     * @param childName  포인트 지급 대상 자녀 이름
     * @param pointDate  포인트 지급 날짜
     * @return 이미 지급된 이력이 있으면 true, 없으면 false
     */
    boolean existsByMemberAndPointTypeAndChildNameAndPointDate(
            Member member,
            PointType pointType,
            String childName,
            LocalDate pointDate
    );
}
