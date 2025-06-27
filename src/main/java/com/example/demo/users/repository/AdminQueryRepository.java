package com.example.demo.users.repository;

import com.example.demo.dto.auth.AdminAuthDTO;

import java.util.Optional;

public interface AdminQueryRepository {
    public Optional<AdminAuthDTO> getAdminAuthDTOByUuid(String uuid);
}
