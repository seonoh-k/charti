package com.example.demo.matching.repository;

import com.example.demo.enums.MatchingStatus;
import com.example.demo.matching.entity.Matching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {

    // 전체 조회
    Page<Matching> findAll(Pageable pageable);
    // 상태값으로 조회
    Page<Matching> findByStatus(MatchingStatus status, Pageable pageable);

    // 전문가별 상담 조회
    Page<Matching> findByExpertId(Long expertId, Pageable pageable);

    // 전문가별 + 상태별 상담 조회
    Page<Matching> findByExpertIdAndStatus(Long expertId, MatchingStatus status, Pageable pageable);

    // 제목 검색용

    // 관리자/전체 검색 (제목)
    Page<Matching> findByTitleContaining(String keyword, Pageable pageable);
    Page<Matching> findByStatusAndTitleContaining(MatchingStatus status, String keyword, Pageable pageable);

    // 전문가 검색 (제목)
    Page<Matching> findByExpertIdAndTitleContaining(Long expertId, String keyword, Pageable pageable);
    Page<Matching> findByExpertIdAndStatusAndTitleContaining(Long expertId, MatchingStatus status, String keyword, Pageable pageable);

    // -----------------------------------
    // 1) 로그인한 부모 → 자녀 상담 전체
    Page<Matching> findByChild_Parent_Id(Long parentId, Pageable pageable);

    // 2) 부모 + 상태
    Page<Matching> findByChild_Parent_IdAndStatus(Long parentId,
                                                  MatchingStatus status,
                                                  Pageable pageable);

    // 3) 부모 + 제목 검색
    Page<Matching> findByChild_Parent_IdAndTitleContaining(Long parentId,
                                                           String keyword,
                                                           Pageable pageable);

    // 4) 부모 + 상태 + 제목 검색
    Page<Matching> findByChild_Parent_IdAndStatusAndTitleContaining(Long parentId,
                                                                    MatchingStatus status,
                                                                    String keyword,
                                                                    Pageable pageable);

    // 5) 부모 + 특정 자녀
    Page<Matching> findByChild_Parent_IdAndChild_Id(Long parentId,
                                                    Long childId,
                                                    Pageable pageable);

    // 6) 부모 + 자녀 + 상태
    Page<Matching> findByChild_Parent_IdAndChild_IdAndStatus(Long parentId,
                                                             Long childId,
                                                             MatchingStatus status,
                                                             Pageable pageable);

    // 7) 부모 + 자녀 + 제목
    Page<Matching> findByChild_Parent_IdAndChild_IdAndTitleContaining(Long parentId,
                                                                      Long childId,
                                                                      String keyword,
                                                                      Pageable pageable);

    // 8) 부모 + 자녀 + 상태 + 제목
    Page<Matching> findByChild_Parent_IdAndChild_IdAndStatusAndTitleContaining(Long parentId,
                                                                               Long childId,
                                                                               MatchingStatus status,
                                                                               String keyword,
                                                                               Pageable pageable);
    // 전문가별 단순 조회
    List<Matching> findAllById(Long id);
}
