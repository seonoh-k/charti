package com.example.demo.users.service;

import com.example.demo.dto.ManagerDTO;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.repository.ExpertRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ExpertService {

    private final ExpertRepository expertRepository;


    /**
     * 현재 신청 상태인 담당자들 조회
     *
     * 1. Pageable 객체 생성
     * 2. page(현재 페이지), size(몇개씩 보여주는지)
     * @param page
     * @param size
     * @param sort
     */
    public void getPendingExpertList(Integer page , Integer size, Sort sort){

        Page<Expert> expertPage = expertRepository.findByIsApprovedFalse(PageRequest.of(page, size).withSort(sort));
    }
    /**
     * 현재 신청 상태인 담당자들 조회(정렬 방식 없으면 최신순)
     * 1. Pageable 객체 생성
     * 2. page(현재 페이지), size(몇개씩 보여주는지)
     * 3. Sort.by(Sort.Order.desc(엔티티 클래스의 필드명))
     * @param page
     * @param size
     */
    public List<ManagerDTO> getPendingExpertList(Pageable pageable){

        Page<Expert> expertPage = expertRepository.findByIsApprovedFalse(pageable);

        int size = expertPage.getSize();
        int number = expertPage.getNumber();
        int totalPages = expertPage.getTotalPages();
        long totalElements = expertPage.getTotalElements();
        int numberOfElements = expertPage.getNumberOfElements();
        log.info("size : " + size);
        log.info("number : " + number);
        log.info("totalPages : " + totalPages);
        log.info("totalElements : " + totalElements);
        log.info("numberOfElements : " + numberOfElements);

        // managerPage.forEach((manager) -> log.info("Test Manager : " + manager));

        // List<ManagerDTO> list = expertPage.map((expert) -> ExpertDTO.fromEntity(expert)).toList();

        // return list;
        return new ArrayList<>();
    }

}
