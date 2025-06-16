package com.example.demo.users.repository;

import com.example.demo.users.entity.Expert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertRepository extends JpaRepository<Expert,Long> {

    Page<Expert> findByIsApprovedFalse(Pageable pageable);

}
