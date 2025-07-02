package com.example.demo.users.repository;

import com.example.demo.dto.ManagerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ManagerQueryRepository {

    Optional<ManagerDTO> getManagerById(Long managerId);

    // role = member, deleted = false, approve = false, orderBy createAt
    Page<ManagerDTO> searchUnapprovedManagerList(String type, String keyword, Pageable pageable);

    // role = member, deleted = false, approve = false, orderBy createAt
    Page<ManagerDTO> getUnapprovedManagerList(Pageable pageable);

    // role = member, deleted = false, approve = false, orderBy createAt
    Page<ManagerDTO> searchApprovedManagerList(String type, String keyword, Pageable pageable);

    // role = member, deleted = false, approve = false, orderBy createAt
    Page<ManagerDTO> getApprovedManagerList(Pageable pageable);

}
