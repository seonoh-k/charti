package com.example.demo.survey.repository;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.DailyAnswer;
import com.example.demo.users.entity.Child;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DailyAnswerRepository extends JpaRepository<DailyAnswer, Long> {
    /**
     * childId, category, ageGroup 조건으로 필터링.
     * 파라미터가 null 이면 해당 조건은 무시합니다.
     * 페이징은 Pageable 에 의해 처리됩니다.
     */
    @Query("""
      select da
      from DailyAnswer da
      join da.survey s
      where (:childId   is null or da.child.id     = :childId)
        and (:category  is null or da.category      = :category)
        and (:ageGroup  is null or s.ageGroup       = :ageGroup)
      order by da.createdAt desc
    """)
    Page<DailyAnswer> findByFilters(
            Long childId,
            SurveyCategory category,
            AgeGroup ageGroup,
            Pageable pageable
    );

    List<DailyAnswer> findByChildIdAndDeletedFalseOrderByCreatedAtDesc(Long childId);

    /**
     * 특정 childId 가 주어진 기간(start~end)에 답변을 남겼는지 여부 확인
     */
    boolean existsByChildIdAndCreatedAtBetween(Long childId,
                                               LocalDateTime start,
                                               LocalDateTime end);

    Page<DailyAnswer> findAllByChildIdAndDeletedFalse(Long childId, Pageable pageable);

    boolean existsByChildAndCreatedAtBetween(Child child, LocalDateTime from, LocalDateTime to);
}
