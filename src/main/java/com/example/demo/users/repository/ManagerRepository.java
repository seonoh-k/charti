package com.example.demo.users.repository;

import com.example.demo.users.entity.Manager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    @Query("SELECT m FROM Users u JOIN u.manager m JOIN m.group g WHERE m.isApproved = false")
    Page<Manager> findByIsApprovedFalse(Pageable pageable);
    // List<Manager> findByIsApprovedFalse();

    @Query("SELECT m FROM Users u JOIN u.manager m JOIN m.group g WHERE m.isApproved = true")
    Page<Manager> findByIsApprovedTrue(Pageable pageable);



}