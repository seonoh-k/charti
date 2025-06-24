package com.example.demo.matching.service;

import com.example.demo.enums.MatchingStatus;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.repository.MatchingRepository;
import jakarta.transaction.Transactional;
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

    public List<Matching> findByStatus(MatchingStatus status) {
        return matchingRepository.findByStatus(status);
    }

    /** 상세 조회*/
    public Optional<Matching> findById(Long id) {
        return matchingRepository.findById(id);
    }
}
