package com.example.demo.matching.service;

import com.example.demo.enums.MatchingStatus;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.repository.MatchingRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 전문가별 단순 조회
    public List<Matching> findAllByExpertId(Long id) { return matchingRepository.findAllByExpertId(id); }

    public Page<Matching> findAll(Pageable pageable) {
        return matchingRepository.findAll(pageable);
    }

    public Page<Matching> findByStatus(MatchingStatus status, Pageable pageable) {
        return matchingRepository.findByStatus(status, pageable);
    }

    /** 전문가별 상담 조회 */
    public Page<Matching> findByExpertId(Long expertId, Pageable pageable) {
        return matchingRepository.findByExpertId(expertId, pageable);
    }

    /** 전문가별 + 상태별 상담 조회 */
    public Page<Matching> findByExpertIdAndStatus(Long expertId, MatchingStatus status, Pageable pageable) {
        return matchingRepository.findByExpertIdAndStatus(expertId, status, pageable);
    }

    /** 상세 조회*/
    public Optional<Matching> findById(Long id) {
        return matchingRepository.findById(id);
    }

    /**
     * 부모용 상담 조회
     * @param parentId 로그인한 부모(Member).id
     * @param status   ALL or REQUESTED/MATCHED/RESPONDED
     * @param childId  (선택) 특정 자녀 필터
     * @param keyword  (선택) 제목 키워드
     */
    public Page<Matching> findForParent(
            Long parentId,
            String status,
            Long childId,
            String keyword,
            Pageable pr) {

        boolean hasChild  = (childId  != null);
        boolean hasStatus = (!"ALL".equals(status));
        boolean hasKey    = (keyword != null && !keyword.isBlank());
        MatchingStatus ms = hasStatus ? MatchingStatus.valueOf(status) : null;

        if (!hasChild && !hasStatus && !hasKey) {
            return matchingRepository.findByChild_Parent_Id(parentId, pr);
        }
        if (!hasChild &&  hasStatus && !hasKey) {
            return matchingRepository.findByChild_Parent_IdAndStatus(parentId, ms, pr);
        }
        if (!hasChild && !hasStatus &&  hasKey) {
            return matchingRepository.findByChild_Parent_IdAndTitleContaining(parentId, keyword, pr);
        }
        if (!hasChild &&  hasStatus &&  hasKey) {
            return matchingRepository.findByChild_Parent_IdAndStatusAndTitleContaining(parentId, ms, keyword, pr);
        }
        if ( hasChild && !hasStatus && !hasKey) {
            return matchingRepository.findByChild_Parent_IdAndChild_Id(parentId, childId, pr);
        }
        if ( hasChild &&  hasStatus && !hasKey) {
            return matchingRepository.findByChild_Parent_IdAndChild_IdAndStatus(parentId, childId, ms, pr);
        }
        if ( hasChild && !hasStatus &&  hasKey) {
            return matchingRepository.findByChild_Parent_IdAndChild_IdAndTitleContaining(parentId, childId, keyword, pr);
        }
        // hasChild && hasStatus && hasKey
        return matchingRepository.findByChild_Parent_IdAndChild_IdAndStatusAndTitleContaining(
                parentId, childId, ms, keyword, pr);
    }

    // 관리자용 제목 검색
    public Page<Matching> findByTitle(String keyword, Pageable pr) {
        return matchingRepository.findByTitleContaining(keyword, pr);
    }
    public Page<Matching> findByStatusAndTitle(String status, String keyword, Pageable pr) {
        MatchingStatus ms = MatchingStatus.valueOf(status);
        return matchingRepository.findByStatusAndTitleContaining(ms, keyword, pr);
    }

    // 전문가용 제목 검색
    public Page<Matching> findByExpertIdAndTitle(Long expertId, String keyword, Pageable pr) {
        return matchingRepository.findByExpertIdAndTitleContaining(expertId, keyword, pr);
    }
    public Page<Matching> findByExpertIdStatusAndTitle(Long expertId, String status, String keyword, Pageable pr) {
        MatchingStatus ms = MatchingStatus.valueOf(status);
        return matchingRepository.findByExpertIdAndStatusAndTitleContaining(expertId, ms, keyword, pr);
    }

}
