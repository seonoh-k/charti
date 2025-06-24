package com.example.demo.users.repository;

import com.example.demo.dto.ChildDTO;
import com.example.demo.entity.QGroup;
import com.example.demo.users.entity.QChild;
import com.example.demo.users.entity.QManager;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChildQueryRepositoryImpl implements ChildQueryRepository{

    private final JPAQueryFactory queryFactory;
    @Override
    public Page<ChildDTO> getChildDTOsByGroupIdOrderByAgeAndName(Long groupId, Pageable pageable) {
        QChild c = QChild.child;

        // 1) content 조회: age 필드는 DTO의 getAge()로 계산하므로 DB에서는 dummy 값
        List<ChildDTO> content = queryFactory
                .select(Projections.fields(
                        ChildDTO.class,
                        c.id,
                        c.birthOrder,
                        c.name,
                        c.nickname,
                        c.weight,
                        c.height,
                        c.gender,
                        c.riskGroup,
                        c.birthday
                ))
                .from(c)
                .where(c.group.id.eq(groupId))
                .orderBy(
                        c.birthday.asc(),    // 생일 빠른 순 → 나이 큰 순 (age desc)
                        c.name.desc()        // 이름 내림차순
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2) total count
        Long total = queryFactory
                .select(c.count())
                .from(c)
                .where(c.group.id.eq(groupId))
                .fetchOne();

        // 3) 계산된 age를 DTO에 채워 넣기
        content.forEach(dto -> dto.setAge(dto.calculateAge()));

        return new PageImpl<>(content, pageable, total);
    }
}
