package com.example.demo.users.repository;

import com.example.demo.users.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExpertRepository extends JpaRepository<Expert,Long> {

    @Modifying
    @Query("UPDATE Users u SET u.role = 'ROLE_EXPERT' WHERE u.id = :id AND u.expert.isApproved = true")
    int approveExpert(Long id);

    Optional<Expert> findByUsersId(Long userId); // 여기서 Users.id로 조회
}