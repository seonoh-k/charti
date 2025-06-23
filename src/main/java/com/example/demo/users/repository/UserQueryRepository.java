package com.example.demo.users.repository;

import com.example.demo.dto.UserAuthDTO;

import java.util.Optional;

public interface UserQueryRepository {
    Optional<UserAuthDTO> findAuthByUuid(String uuid);
}
