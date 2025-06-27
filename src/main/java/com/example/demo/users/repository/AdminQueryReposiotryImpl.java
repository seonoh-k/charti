package com.example.demo.users.repository;

import com.example.demo.dto.auth.AdminAuthDTO;
import com.example.demo.users.entity.QAdmin;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

}
