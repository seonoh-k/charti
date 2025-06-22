package com.example.demo.users.repository;

import com.example.demo.dto.UserAuthDTO;
import com.example.demo.users.entity.QUsers;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository{

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
}
