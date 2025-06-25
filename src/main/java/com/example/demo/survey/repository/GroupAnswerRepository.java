package com.example.demo.survey.repository;

import com.example.demo.survey.entity.GroupAnswer;
import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.TargetGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupAnswerRepository extends JpaRepository<GroupAnswer, Long> {


    @Query(
            "SELECT ga FROM GroupAnswer ga " +
                    "JOIN FETCH ga.surveySet " +
                    "JOIN FETCH ga.child " +
                    "WHERE ga.child.id = :childId AND ga.deleted = false " +
                    "ORDER BY ga.createdAt DESC")
    List<GroupAnswer> findByChildIdWithDetails(@Param("childId") Long childId);

    List<GroupAnswer> findByChildIdAndDeletedFalseOrderByCreatedAtDesc(Long childId);

    Page<GroupAnswer> findByChildIdAndAgeGroupAndTargetGroup(
            Long childId,
            AgeGroup ageGroup,
            TargetGroup targetGroup,
            Pageable pageable);
}
