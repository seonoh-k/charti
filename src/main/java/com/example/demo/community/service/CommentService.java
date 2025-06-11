package com.example.demo.community.service;

import com.example.demo.community.entity.Comment;
import com.example.demo.community.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository repo;
    public CommentService(CommentRepository repo) { this.repo = repo; }

    /**
     * 지정된 게시글 ID에 달린 댓글 전체를 조회하여 오름차순(등록 시간 순)으로 반환합니다.
     */
    public List<Comment> getComments(Long communityId) {
        return repo.findByCommunityIdOrderByCreatedAtAsc(communityId);
    }

    /**
     * 새로운 댓글을 저장합니다.
     */
    @Transactional
    public Comment save(Comment comment) {
        return repo.save(comment);
    }

    /**
     * 지정된 게시글 ID에 달린 댓글을 페이지 단위로 조회합니다.
     * @param communityId 댓글이 속한 게시글 ID
     * @param page 조회할 페이지 번호 (0부터 시작)
     */
    public Page<Comment> getCommentsPage(Long communityId, int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Order.asc("createdAt")));
        return repo.findByCommunityId(communityId, pageable);
    }
}
