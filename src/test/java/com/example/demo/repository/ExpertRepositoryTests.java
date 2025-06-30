package com.example.demo.repository;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.entity.Group;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Member;
import com.example.demo.users.repository.*;
import com.example.demo.users.service.ManagerService;
import groovy.util.logging.Log4j2;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
//@Transactional
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
    private GroupRepository groupRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ManagerQueryRepository managerQueryRepository;

    @Autowired
    private UserQueryRepository userQueryRepository;

    @Autowired
    private ManagerService managerService;


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
//        System.out.println("ExpertRepositoryTests.zz");
//        Pageable pageable  = PageRequest.of(0, 10);
        // Page<ExpertDTO> allApprovedExperts = expertQueryRepository.findAllApprovedExperts(pageable);
        // allApprovedExperts.forEach(System.out::println);
//        Page<ExpertDTO> allUnapprovedExperts = expertQueryRepository.getUnapprovedExpertList(pageable);
//        PagingResultDTO pagingResultDTO = new PagingResultDTO(allUnapprovedExperts);
//        List dtoList = pagingResultDTO.getDtoList();

//        List<Integer> list = new ArrayList<>();
//        boolean b = list.addAll(List.of(38, 39, 40, 41, 42, 43, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100));
//
//
//        for(Integer item : list){
//            Optional<ManagerDTO> managerDTO = managerQueryRepository.getManagerById(item.longValue());
//            if (managerDTO.isPresent()){
//                System.out.println("ExpertRepositoryTests : {"+managerDTO.get()+"}");
//            }
//        }

        Optional<Member> withUsersById = memberRepository.findWithUsersById(60L);
        if (withUsersById.isPresent()){
            System.out.println("ExpertRepositoryTests.zz : " + withUsersById.get().getName());
        }


    }

    @Test
    public void eeee(){

        Long id = 508L;
        Optional<Expert> byId = expertRepository.findById(id);

    }

//    @Test
//    void insertYoungerSiblingsOfKimSeoyeon() {
//        Member parent = memberRepository.getReferenceById(60L);
//        Group group   = groupRepository.getReferenceById(9L);
//
//        LocalDateTime baseBirthday = LocalDateTime.of(2023, 12, 5, 0, 0);
//
//        for (int idx = 1; idx <= 10; idx++) {
//            int birthOrder = 3 + idx;  // 김서연 양이 3번째, 동생부터 4번 시작
//            LocalDateTime birthday = baseBirthday.plusMonths(idx);
//
//            String name = "김동생" + idx;
//            String gender = (idx % 2 == 0) ? "여" : "남";
//
//            Child child = new Child();
//            child.setParent(parent);
//            child.setGroup(group);
//            child.setBirthOrder(birthOrder);
//            child.setName(name);
//            child.setNickname("");      // 필요 시 빈 문자열 또는 null 허용
//            child.setBirthday(birthday);
//            child.setWeight("5kg");     // 기본 weight
//            child.setHeight("60cm");    // 기본 height
//            child.setGender(gender);
//            child.setRiskGroup(false);
//
//            childRepository.save(child);
//        }
//    }
    @Test
    public void test12(){
        PagingRequest pagingRequest = new PagingRequest();
        Pageable pageable = pagingRequest.toPageable();

        Page<UserDTO> deletedUserList = userQueryRepository.getDeletedUserList(pageable);
        PagingResultDTO pagingResultDTO = new PagingResultDTO(deletedUserList);
        List dtoList = pagingResultDTO.getDtoList();
        dtoList.forEach(System.out::println);

    }

    @Test
    public void testUser(){
        // Long idByUsername = userRepository.getIdByUsername("manager1@test.com");
        // System.out.println(idByUsername);
//        Long userId = 44L;
//        Optional<UserDTO> dtoById = userQueryRepository.getDeletedUserDTOById(userId);
        Optional<ManagerDTO> managerById = managerQueryRepository.getManagerById(519L);
        if (managerById.isPresent()){
            String targetGroup = managerById.get().getTargetGroup();
            System.out.println("ExpertRepositoryTests.testUser : " + targetGroup);
        }
    }

    @Test
    public void testSearch(){
        PagingRequest pagingRequest = PagingRequest.builder().page(0).size(10).direction("desc").sort("groupName").build();
        Pageable pageable = pagingRequest.toPageable();
        // Page<ManagerDTO> managerDTOS = managerQueryRepository.searchApprovedManagerList("name", "", pageable);
        PagingResultDTO<ManagerDTO, Manager> name = managerService.searchApprovedManagerList("groupName", "(주)", pageable);
        name.getDtoList().forEach(managerDTO -> {
            System.out.printf("managerDTO[ name : %s , email :  %s, phoneNumber : %s, groupName : %s ] %n",
                    managerDTO.getName(),managerDTO.getUsername(),managerDTO.getPhoneNumber(),managerDTO.getGroupName());
        });


    }


}
