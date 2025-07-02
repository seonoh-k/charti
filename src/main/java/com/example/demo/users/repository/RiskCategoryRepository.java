package com.example.demo.users.repository;

import com.example.demo.users.entity.RiskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskCategoryRepository extends JpaRepository<RiskCategory, Long> {
}
