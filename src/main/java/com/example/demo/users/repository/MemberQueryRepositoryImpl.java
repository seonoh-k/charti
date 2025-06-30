package com.example.demo.users.repository;

import com.example.demo.dto.ChildDTO;
import com.example.demo.dto.MemberDTO;
import com.example.demo.users.entity.QChild;
import com.example.demo.users.entity.QMember;
import com.example.demo.users.entity.QUsers;
import com.example.demo.users.entity.Role;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;




@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository{

    private final JPAQueryFactory queryFactory;

    private final QUsers u = QUsers.users;
    private final QMember m = QMember.member;
    private final QChild c = QChild.child;

    @Override
    public Optional<MemberDTO> getMemberById(Long id) {
        // 1) 플랫하게 조회
        List<Tuple> tuples = queryFactory
                .select(
                        u.id, u.name, u.nickname, u.username,
                        u.phoneNumber, u.provider,
                        m.totalPoint, u.createdAt, u.deleted,
                        c.id, c.birthOrder,
                        c.name, c.nickname, c.weight, c.height,
                        c.gender, c.riskGroup, c.birthday
                )
                .from(u)
                .join(u.member, m)
                .leftJoin(m.children, c)
                .where(
                        u.id.eq(id),
                        u.role.eq(Role.ROLE_MEMBER),
                        u.deleted.isFalse()
                )
                .fetch();


        Map<Long, MemberDTO> map = new LinkedHashMap<>();
        for (Tuple t : tuples) {
            Long memberId = t.get(u.id);
            MemberDTO member = map.computeIfAbsent(memberId, key ->
                    MemberDTO.builder()
                            .id(memberId)
                            .name(t.get(u.name))
                            .nickname(t.get(u.nickname))
                            .username(t.get(u.username))
                            .phoneNumber(t.get(u.phoneNumber))
                            .provider(t.get(u.provider))
                            .totalPoint(t.get(m.totalPoint))
                            .createdAt(t.get(u.createdAt))
                            .deleted(t.get(u.deleted))
                            .children(new ArrayList<>())
                            .build()
            );

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
                member.getChildren().add(child);
            }
        }

        return Optional.ofNullable(map.get(id));
    }

    @Override
    public Page<MemberDTO> getMemberList(Pageable pageable) {
        // 공통 조건
        BooleanExpression baseCond = u.role.eq(Role.ROLE_MEMBER)
                .and(u.deleted.isFalse());

        // 프로젝션 쿼리
        JPAQuery<MemberDTO> query = queryFactory
                .select(Projections.constructor(MemberDTO.class,
                        u.id, u.name, u.nickname,
                        u.username,
                        u.phoneNumber, u.provider,
                        m.totalPoint, u.createdAt,
                        u.deleted))
                .from(u)
                .join(u.member, m)
                .where(baseCond);

        // 전체 카운트
        long total = query.fetchCount();

        // 페이지네이션 적용 후 페치
        List<MemberDTO> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberDTO> searchMemberList(String type, String keyword, Pageable pageable) {
        BooleanExpression baseCond = u.role.eq(Role.ROLE_MEMBER)
                .and(u.deleted.isFalse());

        // 타입별 LIKE 조건
        BooleanBuilder builder = new BooleanBuilder(baseCond);
        String pattern = "%" + keyword.toLowerCase() + "%";
        switch (type) {
            case "name":
                builder.and(u.name.lower().like(pattern));
                break;
            case "email":
                builder.and(u.username.lower().like(pattern));
                break;
            case "phoneNumber":
                builder.and(u.phoneNumber.lower().like(pattern));
                break;
            case "nickname":
                builder.and(u.nickname.lower().like(pattern));
                break;
            default:
                // 필요 시 예외 처리
        }
        // 정렬 기준만 꺼내고 방향은 레포지토리에서 고정 처리
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
            case "nickname":
                orders = new OrderSpecifier<?>[]{
                        u.nickname.asc(),
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

        JPAQuery<MemberDTO> query = queryFactory
                .select(Projections.constructor(MemberDTO.class,
                        u.id, u.name, u.nickname,
                        u.username,
                        u.phoneNumber, u.provider,
                        m.totalPoint, u.createdAt,
                        u.deleted))
                .from(u)
                .join(u.member, m)
                .where(builder)
                .orderBy(orders);

        long total = query.fetchCount();
        List<MemberDTO> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Boolean existsByAddressId(Long addressId) {
        QMember member = QMember.member;
        Integer fetchFirst = queryFactory
                .selectOne()
                .from(member)
                .where(member.address.id.eq(addressId))
                .fetchFirst();

        return fetchFirst != null;
    }

}
