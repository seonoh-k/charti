package com.example.demo.users.service;

import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.ManagerUpdateRequest;
import com.example.demo.entity.Address;
import com.example.demo.entity.Group;
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
        // 1. Firebase 동기화 (공통: 이름, 전화번호)
        UserRecord.UpdateRequest firebaseReq = new UserRecord.UpdateRequest(uid)
                .setDisplayName(req.getName())
                .setPhoneNumber(req.getPhoneNumber());
        // 프로필 이미지가 있으면 설정
//        if (StringUtils.hasText(req.getProfileImage())) {
//            firebaseReq.setPhotoUrl(req.getProfileImage());
//        }
        FirebaseAuth.getInstance().updateUser(firebaseReq);

        // 2. DB 사용자 엔티티 조회
        Users user = userRepository.findByUuid(uid)
                .orElseThrow(() ->
                        new IllegalArgumentException("유효하지 않은 주소 ID: " + req.getAddressId())
                );

        // 3. 공통 필드 업데이트
        user.setName(req.getName());
        user.setNickname(req.getNickname());
        user.setPhoneNumber(req.getPhoneNumber());
//        if (StringUtils.hasText(req.getProfileImage())) {
//            user.setProfileImage(req.getProfileImage());
//        }

        // 4. 주소 변경
        if (req.getAddressId() != null) {
            Address addr = addressRepository.findById(req.getAddressId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("유효하지 않은 주소 ID: " + req.getAddressId())
                    );
            Manager manager = user.getManager();
            if (manager != null && manager.getGroup() != null) {
                Group grp = manager.getGroup();
                grp.setAddress(addr);
                // 그룹 전용 필드
                grp.setGroupName(req.getGroupName());
                grp.setGroupEmail(req.getGroupEmail());
                grp.setGroupPhoneNumber(req.getGroupPhoneNumber());
                grp.setTargetGroup(req.getTargetGroup());
                groupRepository.save(grp);
            }
        }

        // 5. 매니저 엔티티 저장
        Manager manager = user.getManager();
        if (manager != null) {
            managerRepository.save(manager);
        }
    }

}