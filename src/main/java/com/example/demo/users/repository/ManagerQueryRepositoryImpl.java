package com.example.demo.users.repository;

import com.example.demo.dto.ChildDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.entity.QGroup;
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

@Repository
@RequiredArgsConstructor
public class ManagerQueryRepositoryImpl implements ManagerQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ManagerDTO> getManagerById(Long managerId) {
        QManager m = QManager.manager;
        QUsers u = QUsers.users;
        QGroup g = QGroup.group;
        QChild c = QChild.child;

        // 플랫하게 Manager, Group, Child 한 번에 조회
        List<Tuple> tuples = queryFactory
                .select(
                        u.id, u.name, u.username, u.nickname, u.phoneNumber,
                        g.id, g.groupName, g.groupEmail,
                        m.isApproved, u.createdAt, u.deleted,
                        c.id, c.birthOrder, c.name, c.nickname, c.weight, c.height,
                        c.gender, c.riskGroup, c.birthday
                )
                .from(m)
                .join(m.users, u)
                .join(m.group, g)
                .leftJoin(g.children, c) // 연관관계 바로 join!
                .where(m.id.eq(managerId))
                .fetch();

        ManagerDTO managerDTO = null;
        List<ChildDTO> childList = new ArrayList<>();

        for (Tuple t : tuples) {
            if (managerDTO == null) {
                managerDTO = ManagerDTO.builder()
                        .id(t.get(u.id))
                        .name(t.get(u.name))
                        .username(t.get(u.username))
                        .nickname(t.get(u.nickname))
                        .phoneNumber(t.get(u.phoneNumber))
                        .groupId(t.get(g.id))
                        .groupName(t.get(g.groupName))
                        .groupEmail(t.get(g.groupEmail))
                        .isApproved(t.get(m.isApproved))
                        .createdAt(t.get(u.createdAt))
                        .deleted(t.get(u.deleted))
                        .children(childList) // 자녀 리스트 세팅
                        .build();
            }
            Long childId = t.get(c.id);
            if (childId != null) {
                ChildDTO child = ChildDTO.builder()
                        .id(childId)
                        .birthOrder(t.get(c.birthOrder))
                        .name(t.get(c.name))
                        .nickname(t.get(c.nickname))
                        .weight(t.get(c.weight))
                        .height(t.get(c.height))
                        .gender(t.get(c.gender))
                        .riskGroup(t.get(c.riskGroup))
                        .birthday(t.get(c.birthday))
                        .build();
                childList.add(child);
            }
        }

        return Optional.ofNullable(managerDTO);
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
