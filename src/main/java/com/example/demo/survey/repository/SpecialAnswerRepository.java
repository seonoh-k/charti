package com.example.demo.survey.repository;

import com.example.demo.survey.entity.SpecialAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpecialAnswerRepository extends JpaRepository<SpecialAnswer, Long> {
    List<SpecialAnswer> findByChildIdOrderByCreatedAtDesc(Long childId);
}