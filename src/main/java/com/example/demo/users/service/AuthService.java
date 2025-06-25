package com.example.demo.users.service;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.info.*;
import com.example.demo.dto.request.ExpertJoinRequest;
import com.example.demo.dto.request.ManagerJoinRequest;
import com.example.demo.dto.request.MemberJoinRequest;
import com.example.demo.entity.Address;
import com.example.demo.entity.Group;
import com.example.demo.enums.TargetGroup;
import com.example.demo.entity.LoginHistory;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.LoginHistoryRepository;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.*;
import com.example.demo.users.exception.UserAlreadyExistsException;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.StatusCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 로그인, 회원가입, 인증, 인가와 관련된 서비스 작성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final AddressService addressService;
    private final ExpertRepository expertRepository;
    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    private Users commonInfoToEntity(CommonInfo commonInfo){
        Users users = Users.builder()
                .uuid(commonInfo.getUuid())
                .username(commonInfo.getUsername())
                .name(commonInfo.getName())
                .nickname(commonInfo.getNickname())
                .password(commonInfo.getPassword())
                .phoneNumber(commonInfo.getPhoneNumber())
                .provider("LOCAL")
                .providerId(commonInfo.getProviderId())
                .profileImage(commonInfo.getProfileImage())
                .role(Role.ROLE_MEMBER)
                .build();
        return users;
    }

    private Expert expertInfoToEntity(ExpertInfo expertInfo){
        Expert expert = Expert.builder()
                .major(expertInfo.getMajor())
                .license(expertInfo.getLicense())
                .isApproved(false)
                .build();

        return expert;
    }

    private Group groupInfoToEntity(GroupInfo groupInfo){
        TargetGroup tgEnum = TargetGroup.fromValue(groupInfo.getTargetGroup());
        Group group = Group.builder()
                .groupName(groupInfo.getGroupName())
                .groupPhoneNumber(groupInfo.getGroupPhoneNumber())
                .groupEmail(groupInfo.getGroupEmail())
                .targetGroup(tgEnum)
                .build();

        return group;
    }
    private Manager managerInfoToEntity(ManagerInfo managerInfo){
        Manager manager = Manager.builder()
                .isApproved(false)
                .build();
        return manager;
    }

    private Address addressInfoToEntity(AddressInfo addressInfo){
        Address address = Address.builder()
                .zipNum(addressInfo.getZipNum())
                .gugun(addressInfo.getGugun())
                .sido(addressInfo.getSido())
                .dong(addressInfo.getDong())
                .bunji(addressInfo.getBunji())
                .build();
        return address;
    }

    /**
     *
     *
     * isApproved = false 로 시작<br/>
     * 관리자가 승인 시 ROLE_EXPERT로 승격<br/>
     * Address도 함께 등록<br/>
     * Users 생성 → Address 저장 → Member 저장
     *
     *
     * @param expertJoinRequest
     * @param commonInfo
     * @return
     */
    @Transactional
    public AuthStatus createExpertJoinRequest(ExpertJoinRequest expertJoinRequest,CommonInfo commonInfo) throws UserAlreadyExistsException{

        // Users 생성 (role = ROLE_MEMBER)
        // 이메일 중복여부
        boolean isExist = userService.existsByEmail(commonInfo.getUsername());
        if(isExist){
            throw new UserAlreadyExistsException();
        }
        // 유저 생성
        Users users = this.commonInfoToEntity(commonInfo);
        userService.createMember(users);

        // Address 저장
        AddressInfo addressInfo = expertJoinRequest.getAddressInfo();
        Address address = addressService.getAddressById(addressInfo.getAddressId());

        // Expert 저장 (isApproved = false)
        ExpertInfo expertInfo = expertJoinRequest.getExpertInfo();

        Expert expert = new Expert();
        expert.setUsers(users);
        expert.setAddress(address);
        expert.setMajor(expertInfo.getMajor());
        expert.setCareer(expertInfo.getCareer());
        expert.setLicense(expertInfo.getLicense());

        expertRepository.save(expert);
        return AuthStatus.EXPERT_JOIN_REQUEST_SUCCESS;


//        Expert expert = this.expertInfoToEntity(expertInfo);
        // 주소 기입 했으면
        // 당장은 DB 조회 후 id 값을 추출해 저장하기 때문에 필요없는 로직 상황이 바뀌면 쓸수도 있다
//        if(addressInfo.getZipNum() != null){
//            Optional<Address> address = addressService.getAddressByAllFields(addressInfo);
//            if(address.isPresent()) {
//                expert.setAddress(address.get());
//            }
//        }
    }

    /**
     * group(기관), organization(기관명) 필수
     *
     * isApproved = false 로 시작
     *
     * 승인 시 ROLE_MANAGER 로 변경
     *
     * Users.role = ROLE_MEMBER 로 저장
     * @param managerJoinRequest
     * @param commonInfo
     * @return
     */
    @Transactional
    public AuthStatus createManagerJoinRequest(ManagerJoinRequest managerJoinRequest,CommonInfo commonInfo) throws UserAlreadyExistsException{

        // Users 생성 (role = ROLE_MEMBER)

        boolean isExist = userService.existsByEmail(commonInfo.getUsername());
        if(isExist){
            throw new UserAlreadyExistsException();
        }

        Users users = this.commonInfoToEntity(commonInfo);
        userService.createMember(users);

        // Address & Group 저장 절차
        AddressInfo addressInfo = managerJoinRequest.getAddressInfo();
        Address address = addressService.getAddressById(addressInfo.getAddressId());
        GroupInfo groupInfo = managerJoinRequest.getGroupInfo();


        Group group;
        Long groupId = groupInfo.getGroupId();

        if (groupId != null) {
            group = groupRepository.findById(groupId).orElse(null);
            log.info("✅ 기존 그룹 ID {} 사용", groupId);
        } else {
            group = groupInfoToEntity(groupInfo);
            groupRepository.save(group);
            group.setAddress(address);
            log.info("➕ 새로운 그룹 생성: {}", group.getGroupName());
        }

        ManagerInfo managerInfo = managerJoinRequest.getManagerInfo();

        Manager manager = this.managerInfoToEntity(managerInfo);
        manager.setUsers(users);
        manager.setGroup(group);



        managerRepository.save(manager);
        // Users 저장 (role = ROLE_MEMBER)

        // Group or groupId 매핑

        // Manager 저장 (isApproved = false)

        return AuthStatus.MANAGER_JOIN_REQUEST_SUCCESS;
    }

    @Transactional
    public AuthStatus createMemberJoinRequest(MemberJoinRequest memberJoinRequest,CommonInfo commonInfo) {

        // Users 생성 (role = ROLE_MEMBER)
//        CommonInfo commonInfo = memberJoinRequest.getCommonInfo();

        boolean isExist = userService.existsByEmail(commonInfo.getUsername());
        if(isExist){
            throw new UserAlreadyExistsException();
        }
        // users 저장 (isApproved = false)

        Users users = this.commonInfoToEntity(commonInfo);
        userService.createMember(users);

//        // Address 저장
        AddressInfo addressInfo = memberJoinRequest.getAddressInfo();
        //  주소 조회 (addressId 기반)
        Address address = addressService.getAddressById(addressInfo.getAddressId());    // AddressNotFoundException

        Member member = new Member();
        member.setUsers(users);
        member.setAddress(address);


        memberRepository.save(member);

        return AuthStatus.MEMBER_JOIN_REQUEST_SUCCESS;
    }

    public UserDTO getLoginUser() throws UserNotFoundException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String uuid = (String) authentication.getPrincipal();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .get()
                .toString();

        UserDTO userDTO = userService.getMemberByUUID(uuid);

        return userDTO;
    }


    // AuthService 내부에 직접 구현
    public Member getMemberEntityById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("ID " + id + "에 해당하는 멤버를 찾을 수 없습니다."));
    }

    public void createLoginSuccessHistory(String username,String clientIp){

        boolean exists = userRepository.existsByUsername(username);
        Long usersId = null;

        if(exists){
            usersId = userRepository.getIdByUsername(username);
        }

        loginHistoryRepository.save(LoginHistory.builder()
                .userId(usersId)
                .username(username)
                .timestamp(LocalDateTime.now())
                .ipAddress(clientIp)
                .success(true)
                .build());

    }
    public void createLoginFailHistory(String username,String clientIp){

        if(username == null || username.isEmpty() || username.isBlank()){
            loginHistoryRepository.save(LoginHistory.builder()
                    .userId(null)
                    .username(username)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(clientIp)
                    .success(false)
                    .build());
        }

        boolean exists = userRepository.existsByUsername(username);
        if(exists){
            Long usersId = userRepository.getIdByUsername(username);
            loginHistoryRepository.save(LoginHistory.builder()
                    .userId(usersId)
                    .username(username)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(clientIp)
                    .success(false)
                    .build());
        } else{
            loginHistoryRepository.save(LoginHistory.builder()
                    .userId(null)
                    .username(username)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(clientIp)
                    .success(false)
                    .build());
        }


    }

}
