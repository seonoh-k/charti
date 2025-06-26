package com.example.demo.repository;

import com.example.demo.entity.Group;
import com.example.demo.enums.TargetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group,Long> {

    List<Group> findByGroupNameContainingIgnoreCase(String name);
    /**
     * [추가] DB에 존재하는 모든 TargetGroup의 종류를 중복 없이 조회합니다.
     * @return 중복이 제거된 TargetGroup 목록
     */
    @Query("SELECT DISTINCT g.targetGroup FROM Group g WHERE g.targetGroup IS NOT NULL")
    List<TargetGroup> findDistinctTargetGroups();
}
