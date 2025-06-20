package com.example.demo.users.service;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.util.StatusCode;
import com.example.demo.util.UserStatus;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ExpertService {

    private final ExpertRepository expertRepository;

    public PagingResultDTO<ExpertDTO, Expert> getPendingExpertListWithPaging(Pageable pageable) {

        Page<Expert> result = expertRepository.findByIsApprovedFalse(pageable);
        return new PagingResultDTO<>(result, ExpertDTO::fromEntity);
    }
    public PagingResultDTO<ExpertDTO, Expert> getPendingExpertListWithPaging(String type,String keyword,Pageable pageable) {

        Page<Expert> result = expertRepository.findByIsApprovedFalse(pageable);
        return new PagingResultDTO<>(result, ExpertDTO::fromEntity);
    }
    // 미승인 상태 전문가 리스트 -> 리스트 반환
    public List<ExpertDTO> getExpertList(Pageable pageable) {
        Page<Expert> result = expertRepository.findByIsApprovedFalse(pageable);
        List<ExpertDTO> list = result.map(ExpertDTO::fromEntity).toList();
        return list;
    }
    // 승인 상태 전문가 리스트 -> 페이지 DTO
    public PagingResultDTO<ExpertDTO, Expert> getApprovedExpertListWithPaging(Pageable pageable) {
        Page<Expert> result = expertRepository.findByIsApprovedTrue(pageable);
        return new PagingResultDTO<>(result, ExpertDTO::fromEntity);
    }

    // 승인 하기
    @Transactional
    public StatusCode approveExpert(Long id) throws UserNotFoundException{
        int i = 0;
        Optional<Expert> byId = expertRepository.findById(id);

        if(byId.isPresent()){
            i = expertRepository.approveExpert(id);
        } else{
            throw new UserNotFoundException();
        }

        if(i >= 1){
            return UserStatus.APPROVE_SUCCESS;
        } else {
            return UserStatus.APPROVE_FAIL;
        }

    }
    // 여러명 한꺼번에 승인하기
    @Transactional
    public void approveExpertsByIds(List<Long> ids) throws UserNotFoundException{
        for (Long id : ids) {
            Optional<Expert> byId = expertRepository.findById(id);
            if(byId.isPresent()){
                int i = expertRepository.approveExpert(id);
            } else{
                throw new UserNotFoundException();
            }

        }
    }

}