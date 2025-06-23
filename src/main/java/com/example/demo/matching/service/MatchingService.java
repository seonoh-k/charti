package com.example.demo.matching.service;

import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.repository.MatchingRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
}
