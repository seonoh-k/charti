package com.example.demo.users.repository;

import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    // boolean existsByEmailAndName(String email, String name);

    boolean existsByPhoneNumber(String phone);
    public Optional<Users> findByProviderAndProviderId(String provider, String providerId);

    // 이름으로 검색 (대소문자 무시)
    List<Users> findByNameContainingIgnoreCase(String keyword);

    // 닉네임으로 검색 (대소문자 무시)
    List<Users> findByNicknameContainingIgnoreCase(String keyword);

    // 멤버 조회 (삭제 X)
    @Query("SELECT u FROM Users u JOIN u.member m WHERE u.role = com.example.demo.users.entity.Role.ROLE_MEMBER AND u.deleted = false")
    Page<Users> getAllMember(Pageable pageable);
    // 멤버 조회 (삭제 O)
    @Query("SELECT u FROM Users u JOIN u.member m WHERE u.role = com.example.demo.users.entity.Role.ROLE_MEMBER AND u.deleted = true")
    Page<Users> getAllMemberDeleted(Role role,Pageable pageable);

    // 전문가 조회 (삭제 X) 승인 여부와 관계 없이
    @Query("SELECT u FROM Users u JOIN u.expert e WHERE u.role = com.example.demo.users.entity.Role.ROLE_EXPERT AND u.deleted = false")
    Page<Expert> getAllExpert(Pageable pageable);
    // 전문가 조회 (삭제 O) 승인 여부와 관계 없이
    @Query("SELECT u FROM Users u JOIN u.expert e WHERE u.role = com.example.demo.users.entity.Role.ROLE_EXPERT AND u.deleted = true")
    Page<Expert> getAllExpertDeleted(Pageable pageable);

    // 전문가 삭제 X 승인 X
    @Query("SELECT u FROM Users u " +
            " JOIN u.expert e " +
            " WHERE u.role = com.example.demo.users.entity.Role.ROLE_EXPERT " +
            " AND u.deleted = false" +
            " AND e.isApproved = false")
    Page<Expert> getAllExpertUnApproved(Pageable pageable);

    // 전문가 삭제 X 승인 O
    @Query("SELECT u FROM Users u " +
            " JOIN u.expert e " +
            " WHERE u.role = com.example.demo.users.entity.Role.ROLE_EXPERT " +
            " AND u.deleted = false" +
            " AND e.isApproved = true")
    Page<Expert> getAllExpertApproved(Pageable pageable);

    // 전문가 삭제 O 승인 X
    @Query("SELECT u FROM Users u " +
            " JOIN u.expert e " +
            " WHERE u.role = com.example.demo.users.entity.Role.ROLE_EXPERT " +
            " AND u.deleted = true" +
            " AND e.isApproved = false")
    Page<Expert> getAllExpertDeletedAndUnApproved(Pageable pageable);

    // 전문가 삭제 O 승인 O
    @Query("SELECT u FROM Users u " +
            " JOIN u.expert e " +
            " WHERE u.role = com.example.demo.users.entity.Role.ROLE_EXPERT " +
            " AND u.deleted = true" +
            " AND e.isApproved = true")
    Page<Expert> getAllExpertDeletedAndApproved(Pageable pageable);

    // Manager
    // Manager 조회 (삭제 X) 승인과 관계 없이
    @Query("SELECT u FROM Users u JOIN u.manager e WHERE u.role = com.example.demo.users.entity.Role.ROLE_MANAGER AND u.deleted = false")
    Page<Expert> getAllManager(Pageable pageable);
    // Manager 조회 (삭제 O) 승인과 관계 없이
    @Query("SELECT u FROM Users u JOIN u.manager e WHERE u.role = com.example.demo.users.entity.Role.ROLE_MANAGER AND u.deleted = true")
    Page<Expert> getAllManagerDeleted(Pageable pageable);

    // Manager 조회 (삭제 X 승인 X)
    @Query("SELECT u FROM Users u " +
            " JOIN u.manager e " +
            " WHERE u.role = com.example.demo.users.entity.Role.ROLE_MANAGER " +
            " AND u.deleted = false" +
            " AND e.isApproved = false")
    Page<Manager> getAllManagerUnApproved(Pageable pageable);

    // Manager 조회 (삭제 X 승인 O)
    @Query("SELECT u FROM Users u " +
            " JOIN u.manager e " +
            " WHERE u.role = com.example.demo.users.entity.Role.ROLE_MANAGER " +
            " AND u.deleted = false" +
            " AND e.isApproved = true")
    Page<Manager> getAllManagerApproved(Pageable pageable);
    // Manager 조회 (삭제 O 승인 X)
    @Query("SELECT u FROM Users u " +
            " JOIN u.manager e " +
            " WHERE u.role = com.example.demo.users.entity.Role.ROLE_MANAGER " +
            " AND u.deleted = true" +
            " AND e.isApproved = false")
    Page<Manager> getAllManagerDeletedAndUnApproved(Pageable pageable);

    // Manager 조회 (삭제 O 승인 O)
    @Query("SELECT u FROM Users u " +
            " JOIN u.manager e " +
            " WHERE u.role = com.example.demo.users.entity.Role.ROLE_MANAGER " +
            " AND u.deleted = true" +
            " AND e.isApproved = true")
    Page<Manager> getAllManagerDeletedAndApproved(Pageable pageable);


}
