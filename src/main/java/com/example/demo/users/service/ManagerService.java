package com.example.demo.users.service;

import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.ManagerUpdateRequest;
import com.example.demo.entity.Address;
import com.example.demo.entity.Group;
import com.example.demo.enums.TargetGroup;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ManagerQueryRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final ManagerQueryRepository managerQueryRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final GroupRepository groupRepository;

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


    /**
     * 담당자 정보 업데이트 (Firebase + DB)
     * @param req 수정 요청 DTO
     * @param uid 인증된 사용자 UID
     * @throws FirebaseAuthException Firebase 업데이트 실패 시
     */
    @Transactional
    public void updateManager(ManagerUpdateRequest req, String uid) throws FirebaseAuthException {
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

        // 주소/그룹/매니저
        Manager manager = user.getManager();
        if (manager != null && manager.getGroup() != null) {
            Group group = manager.getGroup();
            updateIfPresent(req.getGroupName(), group::setGroupName);
            updateIfPresent(req.getGroupEmail(), group::setGroupEmail);
            updateIfPresent(req.getGroupPhoneNumber(), group::setGroupPhoneNumber);
            if (StringUtils.hasText(req.getTargetGroup())) {
                group.setTargetGroup(TargetGroup.fromValue(req.getTargetGroup()));
            }
            if (req.getAddressId() != null) {
                Address addr = addressRepository.findById(req.getAddressId())
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 주소 ID: " + req.getAddressId()));
                group.setAddress(addr);
            }
            groupRepository.save(group);
        }

        managerRepository.save(manager);
    }

    // 람다 도우미
    private void updateIfPresent(String newValue, Consumer<String> setter) {
        if (StringUtils.hasText(newValue)) setter.accept(newValue);
    }

}