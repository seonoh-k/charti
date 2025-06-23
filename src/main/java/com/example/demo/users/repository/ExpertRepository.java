package com.example.demo.users.repository;

import com.example.demo.users.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExpertRepository extends JpaRepository<Expert,Long> {

    @Modifying
    @Query("UPDATE Expert e SET e.isApproved = true WHERE e.users.id = :id")
    int approveExpert(Long id);


    Optional<Expert> findByUsersId(Long userId); // 여기서 Users.id로 조회
}