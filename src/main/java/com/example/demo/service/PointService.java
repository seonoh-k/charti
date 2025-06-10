package com.example.demo.service;

import com.example.demo.dto.PointHistoryView;
import com.example.demo.entity.Member;
import com.example.demo.entity.PointHistory;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    h.getCreatedAt()
            ));
        }

        return result;
    }



}

