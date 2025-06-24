package com.example.demo.matching.repository;

import com.example.demo.enums.MatchingStatus;
import com.example.demo.matching.entity.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {

    // 상태값으로 필터링
    List<Matching> findByStatus(MatchingStatus status);

}
