package com.example.demo.repository;

import com.example.demo.entity.DailySurvey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySurveyRepository extends JpaRepository<DailySurvey, Integer> {
}
