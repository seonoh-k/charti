package com.example.demo.users.service;

import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ManagerQueryRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.util.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final ManagerQueryRepository managerQueryRepository;
    private final UserRepository userRepository;

    public ManagerDTO getManagerById(Long id) throws UserNotFoundException {
        Optional<ManagerDTO> byId = managerQueryRepository.getManagerById(id);
        if (byId.isEmpty()) {
            throw new UserNotFoundException();
        }
        return byId.get();
    }

    public PagingResultDTO<ManagerDTO, Manager> getApprovedManagerList(Pageable pageable) {

        Page<ManagerDTO> result = managerQueryRepository.getApprovedManagerList(pageable);
        return new PagingResultDTO<>(result);
    }
    public PagingResultDTO<ManagerDTO, Manager> getUnapprovedManagerList(Pageable pageable) {

        Page<ManagerDTO> result = managerQueryRepository.getUnapprovedManagerList(pageable);
        return new PagingResultDTO<>(result);
    }

    public PagingResultDTO<ManagerDTO, Manager> searchApprovedManagerList(String type,String keyword, Pageable pageable){
        Page<ManagerDTO> result = managerQueryRepository.searchApprovedManagerList(type, keyword, pageable);
        return new PagingResultDTO<>(result);
    }

    public PagingResultDTO<ManagerDTO, Manager> searchUnapprovedManagerList(String type,String keyword, Pageable pageable){
        Page<ManagerDTO> result = managerQueryRepository.searchUnapprovedManagerList(type, keyword, pageable);
        return new PagingResultDTO<>(result);
    }
    // 미승인 담당자 리스트 (최근 신청자 순)
    public List<ManagerDTO> getLatestUnapproving(Pageable pageable) {

        Page<ManagerDTO> result = managerQueryRepository.getUnapprovedManagerList(pageable);
        return result.toList();
    }

    @Transactional
    public UserStatus approveManager(Long id) throws UserNotFoundException{

        boolean isExists = managerRepository.existsById(id);

        if(isExists){
            managerRepository.approveManager(id);
            userRepository.updateUserRoleToManager(id);
        } else{
            throw new UserNotFoundException();
        }

        return UserStatus.APPROVE_SUCCESS;
    }

    // 매니저 단일 조회가 없어서 작성
    public Manager get(Long id) {
        return managerRepository.findById(id).get();
    }

}