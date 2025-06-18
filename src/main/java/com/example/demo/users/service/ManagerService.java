package com.example.demo.users.service;

import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.util.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final ManagerRepository managerRepository;

    /**
     * 현재 신청 상태인 담당자들 조회
     *
     * 1. Pageable 객체 생성
     * 2. page(현재 페이지), size(몇개씩 보여주는지)
     * @param page
     * @param size
     * @param sort
     */
    public void getPendingManagerList(Integer page , Integer size, Sort sort){

        Page<Manager> managerPage = managerRepository.findByIsApprovedFalse(PageRequest.of(page, size).withSort(sort));
    }
    /**
     * 현재 신청 상태인 담당자들 조회(정렬 방식 없으면 최신순)
     * 1. Pageable 객체 생성
     * 2. page(현재 페이지), size(몇개씩 보여주는지)
     * 3. Sort.by(Sort.Order.desc(엔티티 클래스의 필드명))
     * @param page
     * @param size
     */
//    public List<ManagerDTO> getPendingManagerList(Pageable pageable){
//
//        Page<Manager> managerPage = managerRepository.findByIsApprovedFalse(pageable);
//
//        int size = managerPage.getSize();
//        int number = managerPage.getNumber();
//        int totalPages = managerPage.getTotalPages();
//        long totalElements = managerPage.getTotalElements();
//        int numberOfElements = managerPage.getNumberOfElements();
//
//        log.info("size : " + size);
//        log.info("number : " + number);
//        log.info("totalPages : " + totalPages);
//        log.info("totalElements : " + totalElements);
//        log.info("numberOfElements : " + numberOfElements);
//
//        // managerPage.forEach((manager) -> log.info("Test Manager : " + manager));
//
//        List<ManagerDTO> list = managerPage.map(ManagerDTO::fromEntity).toList();
//
//        return list;
//    }

    public PagingResultDTO<ManagerDTO, Manager> getPendingManagerListWithPaging(Pageable pageable) {

        Page<Manager> result = managerRepository.findByIsApprovedFalse(pageable);
        return new PagingResultDTO<>(result, ManagerDTO::fromEntity);
    }

    // 미승인 담당자 리스트 (최근 신청자 순)
    public List<ManagerDTO> getManagerList(Pageable pageable) {

        Page<Manager> result = managerRepository.findByIsApprovedFalse(pageable);
        List<ManagerDTO> list = result.map(ManagerDTO::fromEntity).toList();
        return list;
    }
    public PagingResultDTO<ManagerDTO, Manager> getApprovedManagerListWithPaging(Pageable pageable) {
        Page<Manager> result = managerRepository.findByIsApprovedTrue(pageable);
        return new PagingResultDTO<>(result, ManagerDTO::fromEntity);
    }
    @Transactional
    public UserStatus approvePendingManager(Manager ... managers){
        Arrays.stream(managers).forEach(manager -> {
            manager.getUsers().setRole(Role.ROLE_MANAGER);
            manager.setIsApproved(true);
            managerRepository.save(manager);
        });
        return UserStatus.APPROVE_SUCCESS;
    }



}