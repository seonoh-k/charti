package com.example.demo.users.repository;

import com.example.demo.dto.AdminDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.auth.AdminAuthDTO;
import com.example.demo.entity.QGroup;
import com.example.demo.users.entity.QAdmin;
import com.example.demo.users.entity.QManager;
import com.example.demo.users.entity.QUsers;
import com.example.demo.users.entity.Role;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class AdminQueryReposiotryImpl implements AdminQueryRepository{

    private final JPAQueryFactory queryFactory;

    /**
     * uuid로 조회하고 deleted가 false인 Admin을 AdminAuthDTO로 반환
     */
    @Override
    public Optional<AdminAuthDTO> getAdminAuthDTOByUuid(String uuid) {
        QAdmin admin = QAdmin.admin;

        AdminAuthDTO dto = queryFactory
                .select(Projections.constructor(
                        AdminAuthDTO.class,
                        admin.uuid,
                        admin.deleted
                ))
                .from(admin)
                .where(admin.uuid.eq(uuid)
                        .and(admin.deleted.isFalse()))
                .fetchOne();

        return Optional.ofNullable(dto);

    }
    @Override
    public Optional<AdminDTO> getAdminDTOById(Long id){
        QAdmin a = QAdmin.admin;

        AdminDTO dto = queryFactory
                .select(Projections.constructor(
                        AdminDTO.class,
                        a.id,
                        a.name,
                        a.position,
                        a.username,
                        a.phoneNumber,
                        a.role,
                        a.createdAt
                ))
                .from(a)
                .where(a.id.eq(id)
                        .and(a.deleted.isFalse()))
                .fetchOne();

        return Optional.ofNullable(dto);
    }

    @Override
    public Page<AdminDTO> searchAdminList(String type, String keyword, Pageable pageable) {
        QAdmin a = QAdmin.admin;

        // 조건 빌더
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(a.role.eq(Role.ROLE_ADMIN));      // ROLE_MEMBER
        builder.and(a.deleted.isFalse());              // 삭제되지 않은 사용자
        // join 관계는 from/join 절에서 처리할 것이므로 여기에 따로 eq()는 불필요

        if (type != null && keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            switch (type) {
                case "name" ->
                        builder.and(a.name.lower().like(kw));
                case "email" ->
                        builder.and(a.username.lower().like(kw));
                case "phoneNumber" ->
                        builder.and(a.phoneNumber.lower().like(kw));
                case "position" ->
                        builder.and(a.position.lower().like(kw));
            }
        }



        String sortProp = pageable.getSort().isSorted()
                ? pageable.getSort().iterator().next().getProperty()
                : "createdAt";

        OrderSpecifier<?>[] orders;
        switch (sortProp) {
            case "name":
                // 이름은 오름차순
                orders = new OrderSpecifier<?>[]{
                        a.name.asc()
                };
                break;
            case "email":
                // 이메일 → 이름 오름차순
                orders = new OrderSpecifier<?>[]{
                        a.username.asc(),
                        a.name.asc()
                };
                break;
            case "phoneNumber":
                orders = new OrderSpecifier<?>[]{
                        a.phoneNumber.asc(),
                        a.name.asc()
                };
                break;
            case "position":
                orders = new OrderSpecifier<?>[]{
                        a.position.asc(),
                        a.name.asc()
                };
                break;
            case "createdAt":
            default:
                // 생성일은 내림차순
                orders = new OrderSpecifier<?>[]{
                        a.createdAt.desc()
                };
                break;
        }

        // 조회할 컬럼 매핑
        List<AdminDTO> content = queryFactory
                .select(Projections.constructor(
                        AdminDTO.class,
                        a.id,
                        a.name,
                        a.position,
                        a.username,
                        a.phoneNumber,
                        a.createdAt

                ))
                .from(a)
                .where(builder)
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트
        Long total = queryFactory
                .select(a.count())
                .from(a)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<AdminDTO> getAdminList(Pageable pageable) {
        QAdmin a = QAdmin.admin;


        BooleanBuilder builder = new BooleanBuilder();
        builder.and(a.role.eq(Role.ROLE_ADMIN));      // ROLE_MEMBER
        builder.and(a.deleted.isFalse());              // 삭제되지 않은 사용자

        // Sort 처리 (기본: Manager 생성일 내림차순)
        OrderSpecifier<?> orderBy = a.createdAt.desc();
        if (pageable.getSort().isSorted()) {
            Sort.Order sortOrder = pageable.getSort().iterator().next();
            if ("createdAt".equals(sortOrder.getProperty())) {
                orderBy = sortOrder.isAscending() ? a.createdAt.asc() : a.createdAt.desc();
            }
            // 필요하면 u.name.asc() 등의 다른 필드도 추가 가능
        }

        // 2) 실제 데이터 조회 (join: Users → Manager → Group)
        List<AdminDTO> content = queryFactory
                .select(Projections.constructor(
                        AdminDTO.class,
                        a.id,
                        a.name,
                        a.position,
                        a.username,
                        a.phoneNumber,
                        a.createdAt               // 탈퇴 여부
                ))
                .from(a)
                .where(builder)
                .orderBy(orderBy)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3) 전체 개수 조회
        Long total = queryFactory
                .select(a.count())
                .from(a)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

}
