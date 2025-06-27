package com.example.demo.matching.repository;

import com.example.demo.enums.MatchingStatus;
import com.example.demo.matching.entity.Matching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {

    // 전체 조회
    Page<Matching> findAll(Pageable pageable);
    // 상태값으로 조회
    Page<Matching> findByStatus(MatchingStatus status, Pageable pageable);

    // 전문가별 상담 조회
    Page<Matching> findByExpertId(Long expertId, Pageable pageable);

    // 전문가별 + 상태별 상담 조회
    Page<Matching> findByExpertIdAndStatus(Long expertId, MatchingStatus status, Pageable pageable);

    // 전문가별 단순 조회
    List<Matching> findAllById(Long id);
}
