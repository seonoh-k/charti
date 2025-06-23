package com.example.demo.users.service;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ExpertQueryRepository;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.util.StatusCode;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ExpertService {

    private final ExpertRepository expertRepository;
    private final ExpertQueryRepository expertQueryRepository;
    private final UserRepository userRepository;
    private final FirebaseService firebaseService;

    public ExpertDTO getExpertById(Long id) throws UserNotFoundException {
        Optional<ExpertDTO> byId = expertQueryRepository.getExpertById(id);

        if (byId.isEmpty()) {
            throw new UserNotFoundException();
        }
        return byId.get();
    }

    // 승인 상태의 전문가 페이징으로 가져오기
    public PagingResultDTO getApprovedExpertList(Pageable pageable) {

        Page<ExpertDTO> result = expertQueryRepository.getApprovedExpertList(pageable);

        return new PagingResultDTO<>(result);

    }
    // 미승인 상태의 전문가 페이징으로 가져오기
    public PagingResultDTO getUnapprovedExpertList(Pageable pageable) {

        Page<ExpertDTO> result = expertQueryRepository.getUnapprovedExpertList(pageable);

        return new PagingResultDTO<>(result);
    }
    // 승인된 전문가 검색
    public PagingResultDTO searchApprovedExpertList(String type, String keyword,Pageable pageable) {

        Page<ExpertDTO> result = expertQueryRepository.searchApprovedExpertList(type,keyword,pageable);

        return new PagingResultDTO<>(result);
    }
    // 미승인된 전문가 검색
    public PagingResultDTO searchUnapprovedExpertList(String type, String keyword,Pageable pageable) {

        Page<ExpertDTO> result = expertQueryRepository.searchUnapprovedExpertList(type,keyword,pageable);

        return new PagingResultDTO<>(result);
    }


    // 미승인 상태 전문가 리스트 -> 리스트 반환
    public List<ExpertDTO> getLatestUnapproving(Pageable pageable) {
        Page<ExpertDTO> result = expertQueryRepository.getUnapprovedExpertList(pageable);
        return result.toList();
    }

    // 승인 하기
    @Transactional
    public StatusCode approveExpert(Long id) throws UserNotFoundException {


        boolean isExists = expertRepository.existsById(id);

        userRepository.updateUserRoleToExpert(id);
        if(isExists){

            expertRepository.approveExpert(id);
        } else{
            throw new UserNotFoundException();
        }

        return UserStatus.APPROVE_SUCCESS;
    }
    // 여러명 한꺼번에 승인하기
    @Transactional
    public void approveExpertsByIds(List<Long> ids) throws UserNotFoundException{
        for (Long id : ids) {
            boolean isExist = expertRepository.existsById(id);
            if(isExist){

                int i = expertRepository.approveExpert(id);

            } else{
                throw new UserNotFoundException();
            }

        }
    }

}