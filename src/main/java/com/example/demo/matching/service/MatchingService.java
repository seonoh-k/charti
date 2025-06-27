package com.example.demo.matching.service;

import com.example.demo.enums.MatchingStatus;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.repository.MatchingRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MatchingService {
    private final MatchingRepository matchingRepository;

    public MatchingService(MatchingRepository matchingRepository) {
        this.matchingRepository = matchingRepository;
    }

    public Matching save(Matching matching) {
        return matchingRepository.save(matching);
    }

    // 전문가별 단순 조회
    public List<Matching> findAllById(Long id) { return matchingRepository.findAllById(id); }

    public Page<Matching> findAll(Pageable pageable) {
        return matchingRepository.findAll(pageable);
    }

    public Page<Matching> findByStatus(MatchingStatus status, Pageable pageable) {
        return matchingRepository.findByStatus(status, pageable);
    }

    /** 전문가별 상담 조회 */
    public Page<Matching> findByExpertId(Long expertId, Pageable pageable) {
        return matchingRepository.findByExpertId(expertId, pageable);
    }

    /** 전문가별 + 상태별 상담 조회 */
    public Page<Matching> findByExpertIdAndStatus(Long expertId, MatchingStatus status, Pageable pageable) {
        return matchingRepository.findByExpertIdAndStatus(expertId, status, pageable);
    }

    /** 상세 조회*/
    public Optional<Matching> findById(Long id) {
        return matchingRepository.findById(id);
    }
}
