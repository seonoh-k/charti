package com.example.demo.users.service;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.info.*;
import com.example.demo.dto.request.ExpertJoinRequest;
import com.example.demo.dto.request.ManagerJoinRequest;
import com.example.demo.dto.request.MemberJoinRequest;
import com.example.demo.entity.Address;
import com.example.demo.entity.Group;
import com.example.demo.repository.GroupRepository;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.*;
import com.example.demo.users.exception.UserAlreadyExistsException;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.util.AuthStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
        Group group = Group.builder()
                .groupName(groupInfo.getName())
                .groupPhoneNumber(groupInfo.getPhoneNumber())
                .groupEmail(groupInfo.getEmail())
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
     * @return
     */
    @Transactional
    public AuthStatus createExpertJoinRequest(ExpertJoinRequest expertJoinRequest) throws UserAlreadyExistsException{


        // Users 생성 (role = ROLE_MEMBER)
        CommonInfo commonInfo = expertJoinRequest.getCommonInfo();

        boolean isExist = userService.existsByEmail(commonInfo.getUsername());
        if(isExist){
            throw new UserAlreadyExistsException();
        }
        // Expert 저장 (isApproved = false)

        Users users = this.commonInfoToEntity(commonInfo);
        userRepository.save(users);

        // Address 저장
        AddressInfo addressInfo = expertJoinRequest.getAddressInfo();
        ExpertInfo expertInfo = expertJoinRequest.getExpertInfo();
        Expert expert = this.expertInfoToEntity(expertInfo);
        // 주소 기입 했으면
        if(addressInfo.getZipNum() != null){
            Optional<Address> address = addressService.getAddressByAllFields(addressInfo);
            if(address.isPresent()) {
                expert.setAddress(address.get());
            }
        }
        expert.setUsers(users);
        expertRepository.save(expert);


        return AuthStatus.EXPERT_JOIN_REQUEST_SUCCESS;

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
     * @return
     */
    @Transactional
    public AuthStatus createManagerJoinRequest(ManagerJoinRequest managerJoinRequest) throws UserAlreadyExistsException{

        // Users 생성 (role = ROLE_MEMBER)
        CommonInfo common = managerJoinRequest.getCommonInfo();

        boolean isExist = userService.existsByEmail(common.getUsername());
        if(isExist){
            throw new UserAlreadyExistsException();
        }

        Users users = this.commonInfoToEntity(common);
        userRepository.save(users);

        // Address & Group 저장 절차
        AddressInfo addressInfo = managerJoinRequest.getAddressInfo();
        GroupInfo groupInfo = managerJoinRequest.getGroupInfo();
        Group group = this.groupInfoToEntity(groupInfo);

        // 주소 기입 했으면
        if(addressInfo.getZipNum() != null){
            Optional<Address> address = addressService.getAddressByAllFields(addressInfo);
            if(address.isPresent()){
                group.setAddress(address.get());
            }
        }
        groupRepository.save(group);

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
    public AuthStatus createMemberJoinRequest(MemberJoinRequest memberJoinRequest) {

        // Users 생성 (role = ROLE_MEMBER)
        CommonInfo commonInfo = memberJoinRequest.getCommonInfo();

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
        Address address = addressService.getAddressById(addressInfo.getAddressId());

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



}
