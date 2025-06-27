package com.example.demo.repository;

import com.example.demo.entity.AdminActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionHistoryRepository extends JpaRepository<AdminActionHistory,Long> {
}
