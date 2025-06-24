package com.example.demo.users.repository;

import com.example.demo.users.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExpertRepository extends JpaRepository<Expert,Long> {

    @Modifying
    @Query("UPDATE Expert e SET e.isApproved = true WHERE e.users.id = :id")
    int approveExpert(Long id);


    Optional<Expert> findByUsersId(Long userId); // 여기서 Users.id로 조회

    /**
     * 전문가 major 컬럼에 카테고리 displayName(예: "생활습관")이 들어있으므로
     * 이 값을 이용해 조회
     */
    List<Expert> findAllByMajor(String major);

    Optional<Expert> findByUsersUuid(String uuid);
}