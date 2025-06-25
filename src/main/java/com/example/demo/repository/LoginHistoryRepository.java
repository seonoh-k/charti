package com.example.demo.repository;

import com.example.demo.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

}
