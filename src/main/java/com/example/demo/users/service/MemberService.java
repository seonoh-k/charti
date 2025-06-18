package com.example.demo.users.service;

import com.example.demo.dto.MemberDTO;
import com.example.demo.repository.AddressRepository;
import com.example.demo.service.BaseService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MemberService extends BaseService<Member, MemberRepository> {


    public MemberService(MemberRepository memberRepository) {
        super(memberRepository);
    }

//    public List<MemberDTO> getMemberList(Pageable pageable){
//
//        Page<Member> memberPage = this.repository.getMemberListSortByCreatedAt(pageable);
//
//        memberPage.forEach((member) -> log.info(""+member));
//
//        List<MemberDTO> list = memberPage.map(MemberDTO::fromEntity).toList();
//
//        return list;
//    }



}