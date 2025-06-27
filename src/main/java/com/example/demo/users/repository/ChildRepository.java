package com.example.demo.users.repository;

import com.example.demo.entity.Group;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {

    // 특정 부모의 모든 자녀 조회
    List<Child> findByParent(Member parent);

    // 특정 이름이 포함된 자녀 조회 (선택적)
    List<Child> findByNameContaining(String name);

    // 위험군 자녀 조회
    List<Child> findByRiskGroupTrue();

    // 생일 순 정렬
    List<Child> findAllByOrderByBirthdayAsc();

    // 특정 부모의 자녀 중 위험군만 조회 (예시)
    List<Child> findByParentAndRiskGroupTrue(Member parent);

    // Member → Child 관계에서, 현재 로그인한 유저의 Member.id 로 조회
    // child.parent.users.id = ? 인 자녀들을 모두 가져옴
    List<Child> findByParentUsersId(Long usersId);

    /**
     * 특정 기관(Group) ID에 속한 모든 자녀 목록을 조회합니다.
     * @param groupId 기관(Group)의 ID
     * @return 자녀(Child) 목록
     */
    List<Child> findByGroupId(Long id);

    long countByGroup(Group group);
  
    List<Child> findByParent_Id(Long parentId);
}
