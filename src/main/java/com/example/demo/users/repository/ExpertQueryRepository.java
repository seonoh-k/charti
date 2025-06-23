package com.example.demo.users.repository;

import com.example.demo.dto.ExpertDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ExpertQueryRepository {

    public Optional<ExpertDTO> getExpertById(Long userId);

    // deleted = false, approve true, ROLE = EXPERT, OrderBy => createAt[DESC]
    Page<ExpertDTO> getApprovedExpertList(Pageable pageable);

    // 승인된 전문가 검색
    // deleted = false, approve = true, ROLE = EXPERT  OrderBy => createAt[DESC]
    Page<ExpertDTO> searchApprovedExpertList(String type, String keyword, Pageable pageable);
    // 승인된 전문가 검색
    // deleted = false, approve = true, ROLE = EXPERT  OrderBy => createAt[DESC]
    Page<ExpertDTO> searchUnapprovedExpertList(String type, String keyword, Pageable pageable);

    // 미승인된 전문가
    // deleted = false, approve = false, ROLE = MEMBER,  OrderBy => createAt[DESC]
    Page<ExpertDTO> getUnapprovedExpertList(Pageable pageable);

}

