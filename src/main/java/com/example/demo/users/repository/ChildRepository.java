package com.example.demo.users.repository;

import com.example.demo.entity.Group;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {

    // 특정 부모의 모든 자녀 조회
    List<Child> findByParent(Member parent);

    // Member → Child 관계에서, 현재 로그인한 유저의 Member.id 로 조회
    // child.parent.users.id = ? 인 자녀들을 모두 가져옴
    List<Child> findByParentUsersIdAndDeletedFalse(Long usersId);

    /**
     * 특정 기관(Group) ID에 속한 모든 자녀 목록을 조회합니다.
     * @param groupId 기관(Group)의 ID
     * @return 자녀(Child) 목록
     */
    List<Child> findByGroupId(Long id);

    long countByGroup(Group group);
  
    List<Child> findByParent_Id(Long parentId);
    /**
     *  특정 설문 카테고리(SurveyCategory)에서 위험 판정을 받은 모든 자녀를 조회합니다.
     */
    @Query("SELECT c FROM Child c JOIN c.riskCategories rc WHERE rc.surveyCategory = :surveyCategory AND c.parent.users.deleted = false")
    List<Child> findChildrenByRiskCategory(@Param("surveyCategory") SurveyCategory surveyCategory);

    /**
     *  특정 기관 그룹(TargetGroup)에 속한 모든 자녀를 조회합니다.
     */
    @Query("SELECT c FROM Child c WHERE c.group.targetGroup = :targetGroup AND c.parent.users.deleted = false")
    List<Child> findChildrenByTargetGroup(@Param("targetGroup") TargetGroup targetGroup);

    /**
     *  위험군으로 등록된 모든 자녀를 조회합니다. (카테고리 무관)
     */
    List<Child> findByRiskGroupTrueAndParentUsersDeletedFalse();

}
