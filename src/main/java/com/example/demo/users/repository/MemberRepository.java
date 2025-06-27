package com.example.demo.users.repository;

import com.example.demo.dto.MemberDTO;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @EntityGraph(attributePaths = {"users"})
    Optional<Member> findWithUsersById(Long id);

    Page<Member> findDistinctByChildren_Group_Id(Long groupId, Pageable pageable);

    Optional<Member> findByUsersUuid(String name);

}