package com.example.demo.users.repository;

import com.example.demo.dto.AdminDTO;
import com.example.demo.users.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,Long> {


    boolean existsByUsername(String email);


    // 단일 관리자(ID 기준)를 DTO로 조회
    @Query(value = """
        SELECT new com.example.demo.dto.AdminDTO(
            a.id,
            a.name,
            a.position,
            a.username,
            a.password,
            a.phoneNumber,
            a.role.name()
        )
        FROM Admin a
        WHERE a.id = :id
        """, nativeQuery = true)
    Optional<AdminDTO> findAdminDTOById(@Param("id") Long id);

    /**
     * username으로 단일 AdminDTO 조회
     */
    @Query(value = """
        SELECT new com.example.demo.dto.AdminDTO(
            a.id,
            a.name,
            a.position,
            a.username,
            a.password,
            a.phoneNumber,
            a.role.name()
        )
        FROM Admin a
        WHERE a.username = :username
        """,nativeQuery = true)
    Optional<AdminDTO> findAdminDTOByUsername(@Param("username") String username);

    Optional<Admin> existsByUuid(String uuid);
}
