package com.example.demo.users.repository;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.users.entity.QExpert;
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

@Repository
@RequiredArgsConstructor
public class ExpertQueryRepositoryImpl implements ExpertQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ExpertDTO> getExpertById(Long userId) {
        QExpert e = QExpert.expert;
        QUsers u = QUsers.users;

        ExpertDTO dto = queryFactory
                .select(Projections.constructor(
                        ExpertDTO.class,
                        u.id,                  // users_id
                        u.name,                // 이름
                        u.nickname,            // 닉네임
                        u.username,            // 이메일(username)
                        u.phoneNumber,         // 전화번호
                        e.license,             // 자격증
                        e.major,               // 전공
                        e.career,              // 경력
                        u.createdAt,           // 가입일시
                        e.isApproved,          // 승인여부
                        u.deleted              // 삭제여부
                ))
                .from(e)
                .join(e.users, u)
                .where(u.id.eq(userId))
                .fetchOne();

        return Optional.ofNullable(dto);
    }


    /**
     * 1. Expert와 User와 조인 후  승인되지 않은 ExpertDTO를 생성
     * 2. 생성 시간을 기준으로 내림차순 정렬한다.
     * @param pageable
     * @return
     */
    @Override
    public Page<ExpertDTO> getApprovedExpertList(Pageable pageable) {
        QExpert e = QExpert.expert;
        QUsers u = QUsers.users;

        // 정렬 처리 (users.createdAt 기준)
        OrderSpecifier<?> orderSpecifier = u.createdAt.desc();
        if (pageable.getSort().isSorted()) {

            Sort.Order order = pageable.getSort()
                                        .stream()
                                        .findFirst()
                                        .get();

            if (order.getProperty().equals("createdAt")) {
                orderSpecifier = order.isAscending() ? u.createdAt.asc() : u.createdAt.desc();
            }
        }

        List<ExpertDTO> content = queryFactory
                .select(Projections.constructor(
                        ExpertDTO.class,
                        u.id, u.name, u.nickname, u.username, u.phoneNumber,
                        e.license, e.major, e.career,
                        u.createdAt, e.isApproved,u.deleted
                ))
                .from(e)
                .join(e.users, u)
                .where(
                        u.deleted.isFalse(),
                        u.role.eq(Role.ROLE_EXPERT),
                        e.isApproved.isTrue()
                )
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(e.count())
                .from(e)
                .where(
                        u.deleted.isFalse(),
                        u.role.eq(Role.ROLE_EXPERT),
                        e.isApproved.isTrue()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ExpertDTO> searchApprovedExpertList(String type, String keyword, Pageable pageable) {
        QUsers u = QUsers.users;
        QExpert e = QExpert.expert;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(u.deleted.isFalse());   // 삭제되지 않은 사용자
        builder.and(u.role.eq(Role.ROLE_EXPERT));
        builder.and(e.isApproved.isTrue()); // 승인된 전문가만
        builder.and(e.users.eq(u));         // join 조건 명시

        if (type != null && keyword != null && !keyword.isBlank()) {
            String lowerKeyword = "%" + keyword.toLowerCase() + "%";
            switch (type) {
                case "name" -> builder.and(u.name.lower().like(lowerKeyword));
                case "email" -> builder.and(u.username.lower().like(lowerKeyword));
                case "phoneNumber" -> builder.and(u.phoneNumber.lower().like(lowerKeyword));
                case "major" -> builder.and(e.major.lower().like(lowerKeyword));
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
                        u.name.asc()
                };
                break;
            case "email":
                // 이메일 → 이름 오름차순
                orders = new OrderSpecifier<?>[]{
                        u.username.asc(),
                        u.name.asc()
                };
                break;
            case "phoneNumber":
                orders = new OrderSpecifier<?>[]{
                        u.phoneNumber.asc(),
                        u.name.asc()
                };
                break;
            case "major":
                orders = new OrderSpecifier<?>[]{
                        e.major.asc(),
                        u.name.asc()
                };
                break;
            case "createdAt":
            default:
                // 생성일은 내림차순
                orders = new OrderSpecifier<?>[]{
                        u.createdAt.desc()
                };
                break;
        }

        List<ExpertDTO> content = queryFactory
                        .select(Projections.constructor(
                        ExpertDTO.class,
                        u.id, u.name, u.nickname, u.username, u.phoneNumber,
                        e.license, e.major, e.career,
                        u.createdAt, e.isApproved,u.deleted
                ))
                .from(e)
                .join(e.users, u)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orders) // 기본 정렬
                .fetch();

        Long total = queryFactory
                .select(e.count())
                .from(e)
                .join(e.users, u)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ExpertDTO> getUnapprovedExpertList(Pageable pageable) {
        QUsers u = QUsers.users;
        QExpert e = QExpert.expert;

        // 정렬 지정 (예: createdAt 기준, 필요시 동적 처리도 가능)
        OrderSpecifier<?> orderSpecifier = u.createdAt.desc();
        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            if (order.getProperty().equals("createdAt")) {
                orderSpecifier = order.isAscending() ? u.createdAt.asc() : u.createdAt.desc();
            }
        }

        // 본문 조회
        List<ExpertDTO> content = queryFactory
                .select(Projections.constructor(
                        ExpertDTO.class,
                        u.id, u.name, u.nickname, u.username, u.phoneNumber,
                        e.license, e.major, e.career,
                        u.createdAt, e.isApproved,u.deleted
                ))
                .from(u)
                .join(u.expert, e)
                .where(e.isApproved.isFalse(),
                        u.role.eq(Role.ROLE_MEMBER))
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(e.count())
                .from(u)
                .join(u.expert, e)
                .where(e.isApproved.isFalse(),
                        u.role.eq(Role.ROLE_MEMBER))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
    @Override
    public Page<ExpertDTO> searchUnapprovedExpertList(String type, String keyword, Pageable pageable) {
        QUsers u = QUsers.users;
        QExpert e = QExpert.expert;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(u.deleted.isFalse());   // 삭제되지 않은 사용자
        builder.and(u.role.eq(Role.ROLE_MEMBER)); // 미승인
        builder.and(e.isApproved.isFalse()); // 미승인된 전문가만
        builder.and(e.users.eq(u));         // join 조건 명시

        if (type != null && keyword != null && !keyword.isBlank()) {
            String lowerKeyword = "%" + keyword.toLowerCase() + "%";
            switch (type) {
                case "name" -> builder.and(u.name.lower().like(lowerKeyword));
                case "email" -> builder.and(u.username.lower().like(lowerKeyword));
                case "phoneNumber" -> builder.and(u.phoneNumber.lower().like(lowerKeyword));
                case "major" -> builder.and(e.major.lower().like(lowerKeyword));
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
                        u.name.asc()
                };
                break;
            case "email":
                // 이메일 → 이름 오름차순
                orders = new OrderSpecifier<?>[]{
                        u.username.asc(),
                        u.name.asc()
                };
                break;
            case "phoneNumber":
                orders = new OrderSpecifier<?>[]{
                        u.phoneNumber.asc(),
                        u.name.asc()
                };
                break;
            case "major":
                orders = new OrderSpecifier<?>[]{
                        e.major.asc(),
                        u.name.asc()
                };
                break;
            case "createdAt":
            default:
                // 생성일은 내림차순
                orders = new OrderSpecifier<?>[]{
                        u.createdAt.desc()
                };
                break;
        }

        List<ExpertDTO> content = queryFactory
                .select(Projections.constructor(
                        ExpertDTO.class,
                        u.id, u.name, u.nickname, u.username, u.phoneNumber,
                        e.license, e.major, e.career,
                        u.createdAt, e.isApproved,u.deleted
                ))
                .from(e)
                .join(e.users, u)
                .where(builder)
                .orderBy(orders) // 기본 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(e.count())
                .from(e)
                .join(e.users, u)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }



}

