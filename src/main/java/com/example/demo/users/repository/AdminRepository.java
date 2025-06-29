package com.example.demo.users.repository;

import com.example.demo.dto.AdminDTO;
import com.example.demo.users.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,Long> {


    boolean existsByUsername(String email);

    @Query("SELECT a.uuid FROM Admin a WHERE a.id = :id")
    Optional<String> getAdminUUIDById(@Param("id") Long id);

    // 단일 관리자(ID 기준)를 DTO로 조회
    @Query("""
        SELECT new com.example.demo.dto.AdminDTO(
            a.id,
            a.name,
            a.position,
            a.username,
            a.password,
            a.phoneNumber,
            a.role
        )
        FROM Admin a
        WHERE a.id = :id
        """)
    Optional<AdminDTO> getAdminDTOById(@Param("id") Long id);

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
            a.role
        )
        FROM Admin a
        WHERE a.username = :username
        """)
    Optional<AdminDTO> getAdminDTOByUsername(@Param("username") String username);
    @Query(value = """
        SELECT new com.example.demo.dto.AdminDTO(
            a.id,
            a.uuid,
            a.name,
            a.role
        )
        FROM Admin a
        WHERE a.uuid = :uuid
        """)
    Optional<AdminDTO> getAdminDTOByUUIDToAuth(@Param("uuid") String uuid);

    Optional<Admin> existsByUuid(String uuid);

    @Query("SELECT a.id FROM Admin a WHERE a.username = :username")
    Long getIdByUsername(@Param("username") String username);

    /**
     * 비밀번호를 @Charti1234로 초기화 합니다.
     * @param id
     */
    @Modifying
    @Query("UPDATE Admin a SET a.password = :password WHERE a.id = :id")
    void initializePassword(@Param("id") Long id,@Param("password") String password);
}
