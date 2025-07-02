package com.example.demo.service;

import com.example.demo.dto.PointHistoryView;
import com.example.demo.enums.PointType;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.entity.PointHistory;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 포인트 관리 서비스
 * - 포인트 지급, 차감, 이력 조회, 중복 지급 방지 등 포인트 전반 로직을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private final MemberRepository memberRepository;
    private final PointHistoryRepository historyRepository;

    /**
     * [관리자 수동 포인트 변경]
     * - 지정한 회원에게 포인트를 증감시킵니다.
     * - 포인트 이력에 ADMIN 유형으로 저장됩니다.
     *
     * @param memberId   대상 회원 ID
     * @param amount     포인트 증감량
     * @param description 변경 사유 설명
     */
    @Transactional
    public void changePoint(Long memberId, int amount, String description) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        if (member.getTotalPoint() + amount < 0) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        member.setTotalPoint(member.getTotalPoint() + amount);

        PointHistory history = new PointHistory();
        history.setMember(member);
        history.setChangeAmount(amount);
        history.setDescription(description);
        history.setPointType(PointType.ADMIN);
        history.setPointDate(LocalDate.now());

        historyRepository.save(history);
    }

    /**
     * [회원 보유 포인트 조회]
     *
     * @param memberId 회원 ID
     * @return 현재 총 보유 포인트
     */
    public int getCurrentPoint(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return member.getTotalPoint();
    }

    /**
     * [회원 포인트 변동 이력 조회]
     * - 최신순 정렬
     *
     * @param memberId 회원 ID
     * @return 포인트 이력 리스트
     */
    public List<PointHistory> getPointHistory(Long memberId) {
        return historyRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * [포인트 변동 이력 (최근 10건)]
     * - 최근 이력 기준으로 before/after 포인트를 계산해 가공된 결과 반환
     * - 시간순(오래된 → 최신)으로 정렬하여 사용자 UI에 적합한 형태로 반환
     *
     * @param memberId 회원 ID
     * @return 변동 이력 뷰 리스트
     */
    public List<PointHistoryView> getFormattedHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        List<PointHistory> fullListDesc = historyRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        int limit = Math.min(10, fullListDesc.size());

        List<PointHistoryView> result = new ArrayList<>();
        int currentTotalPoint = member.getTotalPoint();


        for (int i = 0; i < limit; i++) {
            PointHistory h = fullListDesc.get(i);
            int afterPoint = currentTotalPoint;
            int beforePoint = afterPoint - h.getChangeAmount();

            result.add(0, new PointHistoryView(
                    beforePoint,
                    afterPoint,
                    h.getChangeAmount(),
                    h.getDescription(),
                    h.getCreatedAt(),
                    h.getPointType()
            ));
            currentTotalPoint = beforePoint;
        }

        return result;
    }

    /**
     * [공통 포인트 지급 처리]
     *
     * @param member      대상 회원
     * @param amount      지급할 포인트
     * @param description 설명 메시지
     * @param pointType   포인트 유형
     * @param child       자녀 정보 (해당 없음 시 null)
     */
    @Transactional
    public void givePoint(Member member, int amount, String description, PointType pointType, Child child) {
        if (member.getTotalPoint() == null) {
            member.setTotalPoint(0);
        }

        member.setTotalPoint(member.getTotalPoint() + amount);

        PointHistory history = new PointHistory();
        history.setMember(member);
        history.setChangeAmount(amount);
        history.setDescription(description);
        history.setPointType(pointType);
        history.setChild(child);
        history.setPointDate(LocalDate.now());

        historyRepository.save(history);
    }

    /**
     * [기록 문진 포인트 지급]
     * - 동일 자녀에 대해 같은 날짜에 이미 포인트가 지급된 경우 중복 방지
     *
     * @param member 보호자
     * @param child  자녀
     * @return 새로 지급되었으면 true, 이미 지급된 경우 false
     */
    @Transactional
    public boolean giveRecordSurveyPointIfEligible(Member member, Child child) {
        LocalDate today = LocalDate.now();

        boolean alreadyGiven = historyRepository.existsByMemberAndPointTypeAndChildAndPointDate(
                member, PointType.RECORD_SURVEY, child, today);

        if (alreadyGiven) {
            log.info("자녀 '{}' (ID: {})에게 오늘 이미 기록 문진 포인트가 지급됨", child.getName(), child.getId());
            return false;
        }

        String description = "기록 문진 포인트 지급: " + child.getName();
        givePoint(member, 10, description, PointType.RECORD_SURVEY, child);
        log.info("자녀 '{}' (ID: {})에게 기록 문진 포인트 10점 지급 완료", child.getName(), child.getId());
        return true;
    }
    /**
     * [데일리 문진 포인트 지급]
     * - 동일 자녀에 대해 같은 날짜에 이미 포인트가 지급된 경우 중복 방지
     *
     * @param member 보호자
     * @param child  자녀
     * @return 새로 지급되었으면 true, 이미 지급된 경우 false
     */
    @Transactional
    public boolean giveDailySurveyPointIfEligible(Member member, Child child) {
        LocalDate today = LocalDate.now();

        boolean alreadyGiven = historyRepository.existsByMemberAndPointTypeAndChildAndPointDate(
                member, PointType.DAILY_SURVEY, child, today);

        if (alreadyGiven) {
            log.info("자녀 '{}'에게 오늘 이미 데일리 문진 포인트 지급 완료", child.getName());
            return false;
        }

        String description = "데일리 문진 포인트 지급: " + child.getName();
        givePoint(member, 10, description, PointType.DAILY_SURVEY, child);
        return true;
    }


    /**
     * [포인트 차감 처리]
     *
     * @param member      대상 회원
     * @param amount      차감할 포인트 (양수)
     * @param description 설명 메시지
     * @param pointType   포인트 유형 (예: SHOP, CONSULT 등)
     */
    @Transactional
    public void usePoint(Member member, int amount, String description, PointType pointType) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감할 포인트는 1 이상이어야 합니다.");
        }

        int currentPoint = member.getTotalPoint() != null ? member.getTotalPoint() : 0;

        if (currentPoint < amount) {
            throw new IllegalStateException("보유 포인트가 부족합니다.");
        }

        member.setTotalPoint(currentPoint - amount);

        PointHistory history = new PointHistory();
        history.setMember(member);
        history.setChangeAmount(-amount);
        history.setDescription(description);
        history.setPointType(pointType);
        history.setPointDate(LocalDate.now());
        history.setChild(null); // 필요시 child 필드 확장 가능

        historyRepository.save(history);
    }
}
