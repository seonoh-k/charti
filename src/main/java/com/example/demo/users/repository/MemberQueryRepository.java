package com.example.demo.users.repository;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.MemberDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberQueryRepository {

    Optional<MemberDTO> getMemberById(Long id);
    // deleted = false, role = ROLE_MEMBER, OrderBy => CreateAt;
    Page<MemberDTO> getMemberList(Pageable pageable);
    // deleted = false, role = ROLE_MEMBER, OrderBy => CreateAt;
    Page<MemberDTO> searchMemberList(String type,String keyword,Pageable pageable);



}
