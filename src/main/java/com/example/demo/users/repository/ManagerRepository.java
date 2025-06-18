package com.example.demo.users.repository;

import com.example.demo.users.entity.Manager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
    Page<Manager> findByIsApprovedFalse(Pageable pageable);

    boolean existsByGroupId(Long groupId);

}
