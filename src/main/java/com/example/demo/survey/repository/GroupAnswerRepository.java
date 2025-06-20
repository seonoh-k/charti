// com.example.demo.survey.repository.GroupAnswerRepository.java
package com.example.demo.survey.repository;

import com.example.demo.survey.entity.GroupAnswer;
import com.example.demo.survey.entity.SpecialAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupAnswerRepository extends JpaRepository<GroupAnswer, Long> {
    List<GroupAnswer> findByChildIdAndDeletedFalseOrderByCreatedAtDesc(Long childId);
    Page<GroupAnswer> findByChildIdAndAgeGroupAndTargetGroup(
            Long childId,
            com.example.demo.enums.AgeGroup ageGroup,
            com.example.demo.enums.TargetGroup targetGroup,
            Pageable pageable);
}
