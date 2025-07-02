package com.example.demo.users.service;

import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.MemberUpdateRequestByAdmin;
import com.example.demo.service.BaseService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.MemberQueryRepository;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.util.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MemberService extends BaseService<Member, MemberRepository> {

    private MemberQueryRepository memberQueryRepository;

    public Member dtoToEntity(MemberDTO memberDTO){
        Member member = Member.builder()
                .id(memberDTO.getId())
                .totalPoint(memberDTO.getTotalPoint())
                .build();
        return member;
    }

    public MemberService(MemberRepository memberRepository, MemberQueryRepository memberQueryRepository) {
        super(memberRepository);
        this.memberQueryRepository = memberQueryRepository;
    }

    public MemberDTO getMemberById(Long id){
        Optional<MemberDTO> memberById = memberQueryRepository.getMemberById(id);

        if (memberById.isEmpty()){
            throw new UserNotFoundException();
        }
        return memberById.get();
    }
    public List<MemberDTO> getLatestSignups(Pageable pageable){
        Page<MemberDTO> result = memberQueryRepository.getMemberList(pageable);
        return result.toList();
    }
    public PagingResultDTO<MemberDTO, Member> getMemberList(Pageable pageable){
        Page<MemberDTO> result = memberQueryRepository.getMemberList(pageable);
        return new PagingResultDTO(result);
    }
    public PagingResultDTO<MemberDTO, Member> searchMemberList(String type, String keyword,Pageable pageable){
        Page<MemberDTO> result = memberQueryRepository.searchMemberList(type,keyword,pageable);
        return new PagingResultDTO(result);
    }
    public Boolean existsByAddressId(Long id){
        return memberQueryRepository.existsByAddressId(id);
    }



}