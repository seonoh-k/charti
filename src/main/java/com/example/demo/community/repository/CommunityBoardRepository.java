package com.example.demo.community.repository;

import com.example.demo.community.entity.CommunityBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityBoardRepository
        extends JpaRepository<CommunityBoard, Long>,
        JpaSpecificationExecutor<CommunityBoard> {
}