package com.example.demo.users.repository;

import com.example.demo.dto.UserDTO;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import jakarta.transaction.Transactional;
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

    @Query("SELECT u.id FROM Users u WHERE u.username = :username")
    Long getIdByUsername(@Param("username") String username);
    /**
     * [그룹 ID로 소속 사용자 전체 조회]
     * 특정 그룹(group_id)에 속한 사용자들을 모두 조회한다.
     *
     * @param groupId 그룹 ID
     * @return 해당 그룹 소속 사용자 리스트
     */
    List<Users> findAllByManager_Group_Id(Long groupId);

    /**
     * 특정 기관(Group) ID에 자녀가 한 명 이상 소속된 모든 부모(Users)를 중복 없이 조회합니다.
     * @param groupId 기관(Group)의 ID
     * @return 부모(Users) 목록
     */
    @Query("SELECT DISTINCT u FROM Users u JOIN u.member.children c WHERE c.group.id = :groupId AND u.deleted = false")
    List<Users> findParentsWithChildrenInGroup(@Param("groupId") Long groupId);

    /**
     * Soft-Deleted 사용자를 복구합니다.
     * <br/>- deleted = false
     * <br/>- deletedAt = null
     * @param usersId 복구할 Users 엔티티의 PK
     * @return 업데이트된 행(row) 수
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE Users u
           SET u.deleted = false,
               u.deletedAt = null
         WHERE u.id = :usersId
    """)
    int restoreUserById(@Param("usersId") Long usersId);
    @Query("SELECT u.deleted FROM Users u WHERE u.id = :usersId")
    Optional<Boolean> checkIsDeletedById(@Param("usersId") Long usersId);
}
