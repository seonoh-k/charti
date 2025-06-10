package com.example.demo.repository;

import com.example.demo.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<PointHistory> findByMemberIdOrderByCreatedAtAsc(Long memberId);
}
