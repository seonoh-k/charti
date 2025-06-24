package com.example.demo.users.repository;

import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * *** 조회 로 검색
 */
public interface UserRepository extends JpaRepository<Users,Long> {

    public Optional<Users> findByUsername(String username);
    public Optional<Users> findByUuid(String uuid);
    public boolean existsByUuid(String uuid);
    public boolean existsByUsername(String email);
    boolean existsByPhoneNumber(String phone);
    public Optional<Users> findByProviderAndProviderId(String provider, String providerId);

    // 이름으로 검색 (대소문자 무시)
    List<Users> findByNameContainingIgnoreCase(String keyword);

    // 닉네임으로 검색 (대소문자 무시)
    List<Users> findByNicknameContainingIgnoreCase(String keyword);

    Optional<Users> findByNameAndPhoneNumber(String name, String phoneNumber);
    Optional<Users> findByUsernameAndPhoneNumber(String username, String phoneNumber);

    @Modifying
    @Query("UPDATE Users u SET u.role = 'ROLE_EXPERT' WHERE u.id = :id")
    int updateUserRoleToExpert(Long id);

    @Modifying
    @Query("UPDATE Users u SET u.role = 'ROLE_MANAGER' WHERE u.id = :id")
    int updateUserRoleToManager(Long id);

    @Query("SELECT u.uuid FROM Users u WHERE u.id = :id")
    String getUuidById(@Param("id") Long id);

}
