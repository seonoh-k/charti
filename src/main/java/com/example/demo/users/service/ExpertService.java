package com.example.demo.users.service;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.ExpertUpdateRequest;
import com.example.demo.dto.request.ManagerUpdateRequest;
import com.example.demo.entity.Address;
import com.example.demo.entity.Group;
import com.example.demo.enums.TargetGroup;
import com.example.demo.repository.AddressRepository;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ExpertQueryRepository;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.util.StatusCode;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
@Slf4j
public class ExpertService {

    private final ExpertRepository expertRepository;
    private final ExpertQueryRepository expertQueryRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
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

    /**
     * 전문가 정보 업데이트 (Firebase + DB)
     * @param req 수정 요청 DTO
     * @param uid 인증된 사용자 UID
     * @throws FirebaseAuthException Firebase 업데이트 실패 시
     */
    @Transactional
    public void updateExpert(ExpertUpdateRequest req, String uid) throws FirebaseAuthException {
        // Firebase 임시계정 삭제
        if (StringUtils.hasText(req.getNewUid())) {
            try {
                FirebaseAuth.getInstance().deleteUser(req.getNewUid());
            } catch (FirebaseAuthException e) {
                log.warn("임시 UID({}) 삭제 실패: {}", req.getNewUid(), e.getMessage());
            }
        }

        // Firebase 동기화 (공통)
        UserRecord.UpdateRequest firebaseReq = new UserRecord.UpdateRequest(uid);
        if (StringUtils.hasText(req.getName())) firebaseReq.setDisplayName(req.getName());
        if (StringUtils.hasText(req.getPhoneNumber())) firebaseReq.setPhoneNumber(req.getPhoneNumber());
        FirebaseAuth.getInstance().updateUser(firebaseReq);

        Users user = userRepository.findByUuid(uid)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 UID: " + uid));

        // 람다 도우미로 값 있는 것만 set
        updateIfPresent(req.getName(), user::setName);
        updateIfPresent(req.getNickname(), user::setNickname);
        updateIfPresent(req.getPhoneNumber(), user::setPhoneNumber);

        // 전공, 경력, 자격증파일
        Expert expert = user.getExpert();
        if (expert != null) {
            updateIfPresent(req.getMajor(), expert::setMajor);
            updateIfPresent(req.getCareer(), expert::setCareer);
            updateIfPresent(req.getLicense(), expert::setLicense);

            if (req.getAddressId() != null) {
                Address addr = addressRepository.findById(req.getAddressId())
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 주소 ID: " + req.getAddressId()));
                expert.setAddress(addr);
            }
        }

        expertRepository.save(expert);
    }

    // 람다 도우미
    private void updateIfPresent(String newValue, Consumer<String> setter) {
        if (StringUtils.hasText(newValue)) setter.accept(newValue);
    }

}