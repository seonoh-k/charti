package com.example.demo.users.repository;

import com.example.demo.dto.UserAuthDTO;
import com.example.demo.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserQueryRepository {
    Optional<UserAuthDTO> findAuthByUuid(String uuid);
    Page<UserDTO> searchDeletedUsers(String type, String keyword, Pageable pageable);
    Page<UserDTO> getDeletedUserList(Pageable pageable);

}
