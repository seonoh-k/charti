package com.example.demo.matching.repository;

import com.example.demo.matching.entity.MatchingAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingAnswerRepository extends JpaRepository<MatchingAnswer, Long> {
    List<MatchingAnswer> findByMatchingId(Long matchingId);
}