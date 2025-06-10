package com.example.demo.users.repository;

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
}
