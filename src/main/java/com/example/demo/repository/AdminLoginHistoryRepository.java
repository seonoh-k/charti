package com.example.demo.repository;

import com.example.demo.entity.AdminLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLoginHistoryRepository extends JpaRepository<AdminLoginHistory,Long> {
}
