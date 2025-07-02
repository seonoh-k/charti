package com.example.demo.users.repository;

import com.example.demo.dto.AdminDTO;
import com.example.demo.dto.auth.AdminAuthDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AdminQueryRepository {
    Optional<AdminAuthDTO> getAdminAuthDTOByUuid(String uuid);
    Optional<AdminDTO> getAdminDTOById(Long id);
    Page<AdminDTO> searchAdminList(String type, String keyword, Pageable pageable);
    Page<AdminDTO> getAdminList(Pageable pageable);

}
