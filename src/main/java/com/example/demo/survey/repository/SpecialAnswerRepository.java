package com.example.demo.survey.repository;

import com.example.demo.survey.entity.SpecialAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpecialAnswerRepository extends JpaRepository<SpecialAnswer, Long> {
    // 삭제되지 않은 답변만 조회
    List<SpecialAnswer> findByChildIdAndDeletedFalseOrderByCreatedAtDesc(Long childId);
    List<SpecialAnswer> findByIdIn(List<Long> ids);
}