package com.example.demo.users.repository;

import com.example.demo.dto.UserAuthDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.users.entity.QUsers;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserAuthDTO> findAuthByUuid(String uuid) {
        QUsers u = QUsers.users;

        UserAuthDTO dto = queryFactory
                .select(Projections.constructor(
                        UserAuthDTO.class,
                        u.uuid,
                        u.deleted
                ))
                .from(u)
                .where(u.uuid.eq(uuid))
                .fetchOne();

        return Optional.ofNullable(dto);
    }

    @Override
    public Page<UserDTO> searchDeletedUsers(String type, String keyword, Pageable pageable) {
        QUsers u = QUsers.users;
        // 1) 동적 검색 조건 빌드
        BooleanBuilder builder = new BooleanBuilder(u.deleted.isTrue());
        if (type != null && keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            switch (type) {
                case "name" -> builder.and(u.name.lower().like(kw));
                case "email" -> builder.and(u.username.lower().like(kw));
                case "phoneNumber" -> builder.and(u.phoneNumber.lower().like(kw));
                // 필요시 다른 타입 추가
            }
        }
        // 2) 고정 정렬: deletedAt 내림차순
        OrderSpecifier<?> order = u.deletedAt.desc();
        // 3) content 조회
        List<UserDTO> content = queryFactory
                .select(Projections.constructor(
                        UserDTO.class,
                        u.id,
                        u.name,
                        u.nickname,
                        u.username,
                        u.phoneNumber,
                        u.provider,
                        u.role.stringValue()
                ))
                .from(u)
                .where(builder)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 4) total count
        Long total = queryFactory
                .select(u.count())
                .from(u)
                .where(builder)
                .fetchOne();

        // 5) PageImpl 생성 후 반환
        return new PageImpl<>(content, pageable, total);
    }
    @Override
    public Page<UserDTO> getDeletedUserList(Pageable pageable) {
        QUsers u = QUsers.users;

        // only users marked deleted = true
        BooleanBuilder where = new BooleanBuilder(u.deleted.isTrue());

        // fixed sort by deletedAt descending
        OrderSpecifier<?> orderByDeletedAtDesc = u.deletedAt.desc();

        // fetch the page content
        List<UserDTO> content = queryFactory
                .select(Projections.constructor(
                        UserDTO.class,
                        u.id,
                        u.name,
                        u.nickname,
                        u.username,
                        u.phoneNumber,
                        u.provider,
                        u.role.stringValue()
                ))
                .from(u)
                .where(where)
                .orderBy(orderByDeletedAtDesc)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count total matching rows
        Long total = queryFactory
                .select(u.count())
                .from(u)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
