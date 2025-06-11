package com.example.demo.community.repository;

import com.example.demo.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByCommunityIdOrderByCreatedAtAsc(Long communityId);
    Page<Comment> findByCommunityId(Long communityId, Pageable pageable);
}
