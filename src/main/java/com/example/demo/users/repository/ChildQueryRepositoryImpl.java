package com.example.demo.users.repository;

import com.example.demo.dto.ChildDTO;
import com.example.demo.entity.QGroup;
import com.example.demo.users.entity.QChild;
import com.example.demo.users.entity.QManager;
import com.querydsl.core.types.Projections;
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

}
