package com.example.demo.repository;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.users.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
class ExpertRepositoryTests {

    @Autowired
    private ExpertQueryRepository expertQueryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChildRepository childRepository;



//    @Test
//    @DisplayName("삭제되지 않고 승인된 전문가 조회")
//    void findApprovedExpertsTest() {
//
//        Pageable pageable = PageRequest.of(0, 10);
//        // Page<Expert> byIsApprovedTrue = expertRepository.findByIsApprovedTrue(pageable);
//        // Page<ExpertDTO> map = byIsApprovedTrue.map(ExpertDTO::fromEntity);
//
//        Page<Expert> allWithUserFetch = expertRepository.findAllWithUserFetch(pageable);
//        Page<ExpertDTO> map = allWithUserFetch.map(ExpertDTO::fromEntity);
//
//        map.forEach(System.out::println);
//
//    }

//    @Test
//    @DisplayName("멤버 조회시 자식 정보도 같이 조회")
//    void findMember(){
//        Pageable pageable = PageRequest.of(0, 10);
//
//        Page<MemberDTO> pagedResult = memberRepository.findAllMembersDTO(pageable);
//
//        // 각 MemberDTO에 자녀 주입
//        pagedResult.forEach(dto -> {
//            List<ChildDTO> children = childRepository.findChildrenDTOByMemberId(dto.getId());
//            dto.setChildren(children);
//        });
//    }
    @Test
    public void zz(){
        System.out.println("ExpertRepositoryTests.zz");
        Pageable pageable  = PageRequest.of(0, 10);
        // Page<ExpertDTO> allApprovedExperts = expertQueryRepository.findAllApprovedExperts(pageable);
        // allApprovedExperts.forEach(System.out::println);
        Page<ExpertDTO> allUnapprovedExperts = expertQueryRepository.getUnapprovedExpertList(pageable);
        PagingResultDTO pagingResultDTO = new PagingResultDTO(allUnapprovedExperts);
        List dtoList = pagingResultDTO.getDtoList();
        dtoList.forEach(System.out::println);
    }
}
