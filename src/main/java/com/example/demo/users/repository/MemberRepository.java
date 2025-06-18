package com.example.demo.users.repository;

import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {
//    @Query("SELECT m FROM Member m JOIN m.users u")
//    Page<Member> getMemberListSortByCreatedAt(Pageable pageable);

//    @Query("SELECT m FROM Member m JOIN m.users u ORDER BY u.createdAt DESC")
//    Page<Member> getMemberListSortByCreatedAt(Pageable pageable);

//    @Query("SELECT m FROM Member m JOIN m.users u ORDER BY u.createdAt DESC")
//    Page<Member> getMemberListSortByCreatedAt(Pageable pageable);

//    @Query("SELECT u FROM Member m JOIN m.users u ORDER BY u.createdAt DESC")
//    Page<Member> getMemberListSortByCreatedAt(Pageable pageable);

//    @Query(
//            value = "SELECT * FROM member m JOIN users u ON m.users_id = u.users_id ORDER BY u.created_at DESC",
//            countQuery = "SELECT COUNT(*) FROM member m JOIN users u ON m.users_id = u.users_id",
//            nativeQuery = true
//    )
//    Page<Users> getMemberListSortByCreatedAt(Pageable pageable);


}