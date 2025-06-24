package com.example.demo.users.repository;

import com.example.demo.dto.ChildDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.entity.QGroup;
import com.example.demo.enums.TargetGroup;
import com.example.demo.users.entity.QChild;
import com.example.demo.users.entity.QManager;
import com.example.demo.users.entity.QUsers;
import com.example.demo.users.entity.Role;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ManagerQueryRepositoryImpl implements ManagerQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ManagerDTO> getManagerById(Long managerId) {
        QManager m = QManager.manager;
        QUsers u = QUsers.users;
        QGroup g = QGroup.group;

        // 1. Manager + User + Group 정보 조회
        Tuple managerTuple = queryFactory
                .select(
                        u.id, u.name, u.username, u.nickname, u.phoneNumber,
                        g.id, g.groupName, g.groupEmail,g.targetGroup.stringValue(),
                        m.isApproved, u.createdAt, u.deleted
                )
                .from(m)
                .join(m.users, u)
                .join(m.group, g)
                .where(m.id.eq(managerId))
                .fetchOne();

        if (managerTuple == null) {
            return Optional.empty();
        }

        Long groupId = managerTuple.get(g.id);

        // 2. 해당 Group ID로 Child 목록 조회
        QChild c = QChild.child;
        List<ChildDTO> childList = queryFactory
                .select(
                        c.id, c.birthOrder, c.name, c.nickname, c.weight,
                        c.height, c.gender, c.riskGroup, c.birthday
                )
                .from(c)
                .where(c.group.id.eq(groupId))
                .fetch()
                .stream()
                .map(child -> ChildDTO.builder()
                        .id(child.get(c.id))
                        .birthOrder(child.get(c.birthOrder))
                        .name(child.get(c.name))
                        .nickname(child.get(c.nickname))
                        .weight(child.get(c.weight))
                        .height(child.get(c.height))
                        .gender(child.get(c.gender))
                        .riskGroup(child.get(c.riskGroup))
                        .birthday(child.get(c.birthday))
                        .build())
                .collect(Collectors.toList());

        // 3. ManagerDTO 생성
        ManagerDTO managerDTO = ManagerDTO.builder()
                .id(managerTuple.get(u.id))
                .name(managerTuple.get(u.name))
                .username(managerTuple.get(u.username))
                .nickname(managerTuple.get(u.nickname))
                .phoneNumber(managerTuple.get(u.phoneNumber))
                .groupId(groupId)
                .groupName(managerTuple.get(g.groupName))
                .groupEmail(managerTuple.get(g.groupEmail))
                .targetGroup(TargetGroup.valueOf(managerTuple.get(g.targetGroup.stringValue())))
                .isApproved(managerTuple.get(m.isApproved))
                .createdAt(managerTuple.get(u.createdAt))
                .deleted(managerTuple.get(u.deleted))
                .children(childList)
                .build();
        return Optional.of(managerDTO);
    }



    @Override
    public Page<ManagerDTO> searchUnapprovedManagerList(String type, String keyword, Pageable pageable) {
        QUsers u = QUsers.users;
        QManager m = QManager.manager;
        QGroup g = QGroup.group;

        // 조건 빌더
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(u.role.eq(Role.ROLE_MEMBER));      // ROLE_MEMBER
        builder.and(u.deleted.isFalse());              // 삭제되지 않은 사용자
        builder.and(m.isApproved.isFalse());           // 승인되지 않은 매니저
        // join 관계는 from/join 절에서 처리할 것이므로 여기에 따로 eq()는 불필요

        if (type != null && keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            switch (type) {
                case "name" ->
                        builder.and(u.name.lower().like(kw));
                case "email" ->
                        builder.and(u.username.lower().like(kw));
                case "phoneNumber" ->
                        builder.and(u.phoneNumber.lower().like(kw));
                case "groupName" ->
                        builder.and(g.groupName.lower().like(kw));
            }
        }

        // 동적 정렬 (예: createdAt 기준 내림차순)
        OrderSpecifier<?> order = u.createdAt.desc();
        if (pageable.getSort().isSorted()) {
            // 필요시 Sort.Order 에 따라 분기 처리
        }

        // 조회할 컬럼 매핑
        List<ManagerDTO> content = queryFactory
                .select(Projections.constructor(
                        ManagerDTO.class,
                        u.id,
                        u.name,
                        u.username,
                        u.nickname,
                        u.phoneNumber,
                        g.id,                       // 그룹 아이디
                        g.groupName,                // 그룹 이름
                        g.groupEmail,               // 그룹 이메일
                        g.targetGroup.stringValue(),// 그룹 분류
                        m.isApproved,
                        u.createdAt,
                        u.deleted                   // 탈퇴 여부
                ))
                .from(u)
                .join(u.manager, m)
                .join(m.group, g)
                .where(builder)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트
        Long total = queryFactory
                .select(u.count())
                .from(u)
                .join(u.manager, m)
                .join(m.group, g)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ManagerDTO> getUnapprovedManagerList(Pageable pageable) {
        QUsers u = QUsers.users;
        QManager m = QManager.manager;
        QGroup g = QGroup.group;

        // 1) Sort 처리 (기본: Manager 생성일 내림차순)
        OrderSpecifier<?> orderBy = u.createdAt.desc();
        if (pageable.getSort().isSorted()) {
            Sort.Order sortOrder = pageable.getSort().iterator().next();
            if ("createdAt".equals(sortOrder.getProperty())) {
                orderBy = sortOrder.isAscending() ? u.createdAt.asc() : u.createdAt.desc();
            }
            // 필요하면 u.name.asc() 등의 다른 필드도 추가 가능
        }

        // 2) 실제 데이터 조회 (join: Users → Manager → Group)
        List<ManagerDTO> content = queryFactory
                .select(Projections.constructor(
                        ManagerDTO.class,
                        u.id,                         // users_id
                        u.name,                       // name
                        u.username,                   // username (email)
                        u.nickname,                   // nickname
                        u.phoneNumber,                // phoneNumber
                        g.id,                          // 그룹 아이디
                        g.groupName,                // 그룹 이름
                        g.groupEmail,               // 그룹 이메일
                        g.targetGroup.stringValue(), // 그룹 분류
                        m.isApproved,                 // isApproved
                        u.createdAt,
                        u.deleted                   // 탈퇴 여부
                ))
                .from(u)
                .join(u.manager, m)
                .join(m.group, g)
                .where(
                        u.role.eq(Role.ROLE_MEMBER), // ROLE => MEMBER
                        m.isApproved.isFalse(),      // 미승인
                        u.deleted.isFalse()          // 삭제되지 않은 사용자
                )
                .orderBy(orderBy)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3) 전체 개수 조회
        Long total = queryFactory
                .select(m.count())
                .from(u)
                .join(u.manager, m)
                .join(m.group, g)
                .where(
                        u.role.eq(Role.ROLE_MEMBER), // ROLE => MEMBER
                        m.isApproved.isFalse(),      // 미승인
                        u.deleted.isFalse()          // 삭제되지 않은 사용자
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ManagerDTO> searchApprovedManagerList(String type, String keyword, Pageable pageable) {
        QUsers u = QUsers.users;
        QManager m = QManager.manager;
        QGroup g = QGroup.group;

        // 조건 빌더
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(u.role.eq(Role.ROLE_MANAGER));      // ROLE_MANAGER
        builder.and(u.deleted.isFalse());              // 삭제되지 않은 사용자
        builder.and(m.isApproved.isTrue());           // 승인된 매니저
        // join 관계는 from/join 절에서 처리할 것이므로 여기에 따로 eq()는 불필요

        if (type != null && keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            switch (type) {
                case "name" ->
                        builder.and(u.name.lower().like(kw));
                case "email" ->
                        builder.and(u.username.lower().like(kw));
                case "phoneNumber" ->
                        builder.and(u.phoneNumber.lower().like(kw));
                case "groupName" ->
                        builder.and(g.groupName.lower().like(kw));
            }
        }

        // 동적 정렬 (예: createdAt 기준 내림차순)
        OrderSpecifier<?> order = u.createdAt.desc();
        if (pageable.getSort().isSorted()) {
            // 필요시 Sort.Order 에 따라 분기 처리
        }

        // 조회할 컬럼 매핑
        List<ManagerDTO> content = queryFactory
                .select(Projections.constructor(
                        ManagerDTO.class,
                        u.id,
                        u.name,
                        u.username,
                        u.nickname,
                        u.phoneNumber,
                        g.id,                       // 그룹 아이디
                        g.groupName,                // 그룹 이름
                        g.groupEmail,               // 그룹 이메일
                        g.targetGroup.stringValue(), // 그룹 분류
                        m.isApproved,
                        u.createdAt,
                        u.deleted                   // 탈퇴 여부
                ))
                .from(u)
                .join(u.manager, m)
                .join(m.group, g)
                .where(builder)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트
        Long total = queryFactory
                .select(u.count())
                .from(u)
                .join(u.manager, m)
                .join(m.group, g)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ManagerDTO> getApprovedManagerList(Pageable pageable) {
        QUsers u = QUsers.users;
        QManager m = QManager.manager;
        QGroup g = QGroup.group;

        // 1) Sort 처리 (기본: Manager 생성일 내림차순)
        OrderSpecifier<?> orderBy = u.createdAt.desc();
        if (pageable.getSort().isSorted()) {
            Sort.Order sortOrder = pageable.getSort().iterator().next();
            if ("createdAt".equals(sortOrder.getProperty())) {
                orderBy = sortOrder.isAscending() ? u.createdAt.asc() : u.createdAt.desc();
            }
            // 필요하면 u.name.asc() 등의 다른 필드도 추가 가능
        }

        // 2) 실제 데이터 조회 (join: Users → Manager → Group)
        List<ManagerDTO> content = queryFactory
                .select(Projections.constructor(
                        ManagerDTO.class,
                        u.id,                         // users_id
                        u.name,                       // name
                        u.username,                   // username (email)
                        u.nickname,                   // nickname
                        u.phoneNumber,                // phoneNumber
                        g.id,                       // 그룹 아이디
                        g.groupName,                // 그룹 이름
                        g.groupEmail,               // 그룹 이메일
                        g.targetGroup.stringValue(), // 그룹 분류
                        m.isApproved,                 // isApproved
                        u.createdAt,
                        u.deleted                   // 탈퇴 여부
                ))
                .from(u)
                .join(u.manager, m)
                .join(m.group, g)
                .where(
                        u.role.eq(Role.ROLE_MANAGER), // ROLE => MANAGER
                        u.deleted.isFalse(),          // 삭제되지 않은 사용자
                        m.isApproved.isTrue()         // 승인
                )
                .orderBy(orderBy)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3) 전체 개수 조회
        Long total = queryFactory
                .select(m.count())
                .from(u)
                .join(u.manager, m)
                .join(m.group, g)
                .where(
                        u.role.eq(Role.ROLE_MANAGER), // ROLE => MANAGER
                        u.deleted.isFalse(),          // 삭제되지 않은 사용자
                        m.isApproved.isTrue()         // 승인
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
