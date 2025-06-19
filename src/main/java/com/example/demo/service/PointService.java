package com.example.demo.service;

import com.example.demo.dto.PointHistoryView;
import com.example.demo.enums.PointType;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.entity.PointHistory;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final PointHistoryRepository historyRepository;

    @Transactional
    public void changePoint(Long memberId, int amount, String description) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        if (member.getTotalPoint() + amount < 0) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        // 총 포인트 업데이트
        member.setTotalPoint(member.getTotalPoint() + amount);

        // 포인트 히스토리 생성
        PointHistory history = new PointHistory();
        history.setMember(member);
        history.setChangeAmount(amount);
        history.setDescription(description);

        // 저장
        historyRepository.save(history);
        memberRepository.save(member); // 생략해도 persist context에 의해 자동 업데이트되긴 함
    }

    // 조회
    public int getCurrentPoint(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return member.getTotalPoint();
    }

    // 변동 내역 조회
    public List<PointHistory> getPointHistory(Long memberId) {
        return historyRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    public List<PointHistoryView> getFormattedHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        List<PointHistory> fullList = historyRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        int limit = Math.min(10, fullList.size());

        // 최근 10건을 ASC 순으로 재정렬
        List<PointHistory> recent = new ArrayList<>(fullList.subList(0, limit));
        Collections.reverse(recent); // 최신순 → 오래된 순

        List<PointHistoryView> result = new ArrayList<>();
        int current = member.getTotalPoint();

        for (int i = recent.size() - 1; i >= 0; i--) {
            PointHistory h = recent.get(i);
            int after = current;
            current -= h.getChangeAmount();
            result.add(0, new PointHistoryView(
                    current,
                    after,
                    h.getChangeAmount(),
                    h.getDescription(),
                    h.getCreatedAt(),
                    h.getPointType()
            ));
        }

        return result;
    }

    /**
     * [공통 포인트 지급 메서드]
     *
     * 회원(Member)에게 포인트를 지급하고, 지급 이력을 PointHistory에 저장합니다.
     *
     * 특징
     * - 포인트 지급 이유를 pointType(enum)으로 명확히 분리 (ex: RECORD_SURVEY, DAILY_SURVEY 등)
     * - 자녀별 지급 구분을 위해 childName을 저장
     * - 지급 날짜(LocalDate.now()) 기준으로 pointDate 기록
     *
     * 사용 예시
     * pointService.givePoint(member, 10, "기록문진 포인트 지급", PointType.RECORD_SURVEY, "길동이");
     *
     * @param member      포인트를 지급할 회원
     * @param amount      지급할 포인트 양 (정수, 음수도 가능)
     * @param description 관리자 또는 사용자에게 보이는 포인트 설명
     * @param pointType   포인트 지급 유형 (enum으로 구분)
     * @param childName   자녀 이름 (자녀별 지급 이력 추적용)
     */
    @Transactional
    public void givePoint(Member member, int amount, String description, PointType pointType, String childName) {
        if (member.getTotalPoint() == null) {
            member.setTotalPoint(0);
        }

        // 포인트 증가
        member.setTotalPoint(member.getTotalPoint() + amount);

        // 포인트 이력 저장
        PointHistory history = new PointHistory();
        history.setMember(member);
        history.setChangeAmount(amount);
        history.setDescription(description);
        history.setPointType(pointType);
        history.setChildName(childName);
        history.setPointDate(LocalDate.now());

        historyRepository.save(history);
        memberRepository.save(member); // dirty checking으로 생략 가능하나 명시적으로 저장
    }

    /**
     * [기록문진 포인트 지급 - 중복 방지 포함]
     * - 같은 자녀에게 같은 날 RECORD_SURVEY 포인트가 이미 지급되었다면 지급하지 않음
     */
    @Transactional
    public void giveRecordSurveyPointIfEligible(Member member, Child child) {
        LocalDate today = LocalDate.now();

        boolean alreadyGiven = historyRepository.existsByMemberAndPointTypeAndChildNameAndPointDate(
                member, PointType.RECORD_SURVEY, child.getName(), today);

        if (alreadyGiven) return;

        String description = "기록문진 포인트 지급: " + child.getName();
        givePoint(member, 10, description, PointType.RECORD_SURVEY, child.getName());
    }

    /**
     * [포인트 차감 기능]
     * - 회원이 포인트를 사용하는 경우 (ex. 전문가 상담, 상점 등)
     * - 차감 금액이 현재 보유 포인트보다 크면 예외 발생
     * - 포인트 이력을 음수(changeAmount = -금액)로 기록하고,
     *   pointType 및 사유(description)도 함께 저장
     *
     * @param member      포인트를 차감할 회원
     * @param amount      차감할 포인트 금액 (양수로 전달)
     * @param description 포인트 사용 사유 (ex. "전문가 상담 신청")
     * @param pointType   포인트 사용 유형 (ex. CONSULT, SHOP)
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

        // 🔻 포인트 차감
        member.setTotalPoint(currentPoint - amount);

        // 📝 이력 생성 (음수로 기록)
        PointHistory history = new PointHistory();
        history.setMember(member);
        history.setChangeAmount(-amount);            // 차감은 음수
        history.setDescription(description);         // 사유 기록
        history.setPointType(pointType);             // 차감 유형 (enum)
        history.setPointDate(LocalDate.now());       // 오늘 날짜 기준 기록

        // 💾 저장
        historyRepository.save(history);
        memberRepository.save(member); // 실제로는 persist context로 자동 반영되지만 명시적으로 저장
    }


}

