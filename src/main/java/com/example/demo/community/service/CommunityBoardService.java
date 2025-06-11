package com.example.demo.community.service;

import com.example.demo.community.entity.CommunityBoard;
import com.example.demo.community.repository.CommunityBoardRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommunityBoardService {

    private final CommunityBoardRepository repo;

    public CommunityBoardService(CommunityBoardRepository repo) {
        this.repo = repo;
    }

    /**
      * 게시글 목록 조회 (상위카테고리, 하위카테고리, 연령대, 제목, 정렬, 페이징)
      */
      public Page<CommunityBoard> getList(
            String mainCategory,
            String subCategory,
            String ageGroup,
            String keyword,
            String sortOrder,
            int page
    ) {
        Sort sort = "popular".equals(sortOrder)
                ? Sort.by(Sort.Order.desc("views"))
                : Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(page, 10, sort);

        Specification<CommunityBoard> spec = Specification.where(null);

        spec = spec.and((r, q, cb) -> cb.equal(r.get("category"), mainCategory));

        if (!"전체".equals(subCategory)) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("category2"), subCategory));
        }
        if (!"상관없음".equals(ageGroup)) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("ageGroup"), ageGroup));
        }
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((r, q, cb) -> cb.like(r.get("title"), "%" + keyword + "%"));
        }
        return repo.findAll(spec, pageable);
    }

    /**
     * 게시글을 새로 저장하거나 수정된 내용을 업데이트합니다.
     */
    @Transactional
    public CommunityBoard save(CommunityBoard board) {
        return repo.save(board);
    }

    /**
     * 단일 게시글을 ID로 조회합니다.
     * 없으면 RuntimeException을 던집니다.
     */
    public CommunityBoard findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다. id=" + id));
    }

    /**
     * 지정된 ID의 게시글을 삭제합니다.
     */
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
