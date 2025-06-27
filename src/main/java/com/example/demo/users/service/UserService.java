package com.example.demo.users.service;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.*;
import com.example.demo.entity.Address;
import com.example.demo.entity.Group;
import com.example.demo.exception.FirebaseAuthenticationException;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.users.entity.*;
import com.example.demo.users.exception.UserIsNotDeletedException;
import com.example.demo.users.repository.*;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.users.exception.UserNotFoundException;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 유저에 공통으로 들어가는 정보만 필요한 경우 해당 클래스를 사용하여 데이터베이스에서 데이터를 가져오세요.
 * 기타 세분화된 정보가 필요한 경우 각 해당 클래스 서비스를 참조하세요.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {


    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final ExpertRepository expertRepository;
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupRepository groupRepository;
    private final FirebaseService firebaseService;
    private final UserQueryRepository userQueryRepository;



    public UserDTO entityToDTO(Users users) {

        if (users == null) return null;

        return UserDTO.builder()
                .id(users.getId())
                .username(users.getUsername())
                .nickname(users.getNickname())
                .uuid(users.getUuid())
                .password(users.getPassword())
                .name(users.getName())
                .role(users.getRole().name())
                .phoneNumber(users.getPhoneNumber())
                .provider(users.getProvider())
                .providerId(users.getProviderId())
                .build();
    }
    public Users dtoToEntity(UserDTO dto) {

        if (dto == null) return null;

        return Users.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .name(dto.getName())
                .uuid(dto.getUuid())
                .password(dto.getPassword())
                .role(Role.valueOf(dto.getRole()))
                .phoneNumber(dto.getPhoneNumber())
                .provider(dto.getProvider())
                .providerId(dto.getProviderId())
                .build();
    }


    /**
     * 멤버를 생성하고 Enum 타입의 객체를 반환한다.
     *
     * <br/>반환값 참고 -> {@link UserStatus}
     *
     * @param userDTO : 저장할 UserDTO 객체
     * @return UserStatus : 상태와 메세지를 담고있다.
     *
     */
    @Transactional
    public UserStatus createMember(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());
        if(byUsername.isEmpty()){
            // Join Logic
            String rawPassword= userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(rawPassword);
            userDTO.setPassword(encodedPassword);
            userDTO.setRole(Role.ROLE_MEMBER.name());
            userDTO.setPhoneNumber(userDTO.getPhoneNumber());

            userRepository.save(this.dtoToEntity(userDTO));


            return UserStatus.JOIN_SUCCESS;
        } else{
            return UserStatus.JOIN_FAIL;
        }
    }
    /**
     * 멤버를 생성하고 Enum 타입의 객체를 반환한다.
     *
     * <br/>반환값 참고 -> {@link UserStatus}
     *
     * @param users : 저장할 UserDTO 객체
     * @return UserStatus : 상태와 메세지를 담고있다.
     *
     */
    @Transactional
    public UserStatus createMember(Users users){

        Optional<Users> byUsername = userRepository.findByUsername(users.getUsername());
        // 아이디가 없을때
        if(byUsername.isEmpty()){
            // Join Logic 비밀번호 암호화
            String rawPassword= users.getPassword();
            String encodedPassword = passwordEncoder.encode(rawPassword);
            users.setPassword(encodedPassword);

            userRepository.save(users);


            return UserStatus.JOIN_SUCCESS;
        } else{
            return UserStatus.JOIN_FAIL;
        }
    }
    /**
     * 소셜로그인 멤버를 생성하고 Enum 타입의 객체를 반환한다.
     *
     * <br/>반환값 참고 -> {@link UserStatus}
     *
     * @param userDTO : 저장할 UserDTO 객체
     * @return UserStatus : 상태와 메세지를 담고있다.
     *
     */
    @Transactional
    public UserStatus createSocialMember(UserDTO userDTO) {
        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());
        if(byUsername.isEmpty()){
            // Join Logic
            userDTO.setRole(Role.ROLE_MEMBER.name());

            // 1. Users 생성
            Users savedUser = userRepository.save(this.dtoToEntity(userDTO));
            // 2. Member 엔티티 생성 (Users의 id를 FK로)
            Member member = new Member();
            member.setUsers(savedUser);
            memberRepository.save(member);


            return UserStatus.JOIN_SUCCESS;
        } else{
            return UserStatus.JOIN_FAIL;
        }
    }

    @Transactional
    public UserDTO changeRoleToAdmin(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());

        if(byUsername.isEmpty()){
            throw new UserNotFoundException("MEMBER NOT FOUND");
        } else{
            Users users = byUsername.get();
            users.setRole(Role.ROLE_ADMIN);
            Users saved = userRepository.save(users);
            return this.entityToDTO(saved);
        }
    }
    @Transactional
    public UserDTO changeRoleToManager(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());

        if(byUsername.isEmpty()){
            throw new UserNotFoundException("MEMBER NOT FOUND");
        } else{
            Users users = byUsername.get();
            users.setRole(Role.ROLE_MANAGER);
            Users saved = userRepository.save(users);
            return this.entityToDTO(saved);
        }
    }
    @Transactional
    public UserDTO changeRoleToExpert(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());

        if(byUsername.isEmpty()){
            throw new UserNotFoundException("MEMBER NOT FOUND");
        } else{
            Users users = byUsername.get();
            users.setRole(Role.ROLE_EXPERT);
            Users saved = userRepository.save(users);
            return this.entityToDTO(saved);
        }
    }
    @Transactional
    public UserDTO changeRoleToMember(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());

        if(byUsername.isEmpty()){
            throw new UserNotFoundException("MEMBER NOT FOUND");
        } else{
            Users users = byUsername.get();
            users.setRole(Role.ROLE_MEMBER);
            Users saved = userRepository.save(users);
            return this.entityToDTO(saved);
        }
    }


    /**
     * 이메일로 유저가 데이터베이스에 존재하는지 확인한다.
     *
     * @param email : 확인할 이메일
     * @return true : 존재함, false : 존재하지 않음
     *
     */
    public boolean existsByEmail(String email){
        return userRepository.existsByUsername(email);
    }

    /**
     * 전화번호로 유저가 데이터베이스에 존재하는지 확인한다.
     *
     * @param phoneNumber : 확인할 전화번호
     * @return true : 존재함, false : 존재하지 않음
     */
    public boolean existsByPhoneNumber(String phoneNumber){
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     *
     * @param provider
     * @param providerId
     * @return
     */
    public Optional<Users> findByProviderAndProviderId(String provider, String providerId){
        return userRepository.findByProviderAndProviderId(provider,providerId);
    }

    /**
     * 이메일로 멤버를 가져온다.
     *
     * @param email : 확인할 이메일
     * @return UserDTO: Member Entity객체를 변환하여 UserDTO객체로 반환한다.
     * @throws UserNotFoundException 해당하는 유저가 없는 경우 발생
     */
    public UserDTO getMemberByEmail(String email) {
        Users user = userRepository.findByUsername(email)
                .orElseThrow(() -> new UserNotFoundException("유저가 없어요"));

        if (user.isDeleted()) {
            throw new IllegalStateException("탈퇴한 사용자입니다.");
        }

        return this.entityToDTO(user);
    }

    /**
     * UUID로 유저를 가져온다.
     *
     * @param uuid : 확인할 uuid
     * @return UserDTO : Member Entity객체를 변환하여 UserDTO객체로 반환한다.
     * @throws UserNotFoundException 해당하는 유저가 없는 경우 발생
     */
    public UserDTO getMemberByUUID(String uuid){
        Optional<Users> byUuid = userRepository.findByUuid(uuid);
        if(byUuid.isPresent()){
            return this.entityToDTO(byUuid.get());
        } else{
            throw new UserNotFoundException("유저가 없어요");
        }
    }

    @Transactional
    public void updateMemberByAdmin(MemberUpdateRequestByAdmin request) throws FirebaseAuthenticationException{
        // 1) Users 프록시 로드 (SELECT 없이)
        Users user = userRepository.getReferenceById(request.getId());

        // 2) 허용된 필드만 반영
        user.setName(request.getName());
        user.setNickname(request.getNickname());

    }

    @Transactional
    public void updateExpertByAdmin(ExpertUpdateRequestByAdmin request) throws FirebaseAuthenticationException{
        // 1) Users 프록시 로드 (SELECT 없이)
        Users user = userRepository.getReferenceById(request.getId());
        // 2) 수정 허용 필드만 반영
        user.setName(request.getName());
        user.setNickname(request.getNickname());

        // 3) Expert 프록시 로드
        Expert expert = expertRepository.getReferenceById(request.getId());

    }

    @Transactional
    public void updateManagerByAdmin(ManagerUpdateRequestByAdmin request) throws FirebaseAuthenticationException{
        // 1) Users 프록시만 로드
        Users user = userRepository.getReferenceById(request.getId());
        user.setName(request.getName());
        user.setNickname(request.getNickname());

        // 2) Manager 프록시 로드
        Manager manager = managerRepository.getReferenceById(request.getId());

        // 3) Group 프록시 로드
        Long groupId = manager.getGroup().getId();
        Group group = groupRepository.getReferenceById(groupId);


    }
    @Transactional
    public void updateMemberBySuperAdmin(MemberUpdateRequestBySuperAdmin request) throws FirebaseAuthenticationException{
        // 1) Users 프록시 로드 (SELECT 없이)
        Users user = userRepository.getReferenceById(request.getId());

        // 2) 허용된 필드만 반영
        user.setName(request.getName());
        user.setNickname(request.getNickname());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProvider(request.getProvider());


        try{
            String proxyUUID = user.getUuid();
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                // 변경이 감지되면, 원하는 초기값으로 설정
                String DEFAULT_PW = "Charti@1234";  // 예: 초기 비밀번호 상수
                firebaseService.initFirebasePassword(proxyUUID);
                user.setPassword(passwordEncoder.encode(DEFAULT_PW));
            }
        } catch (FirebaseAuthException firebaseAuthException){
            throw new FirebaseAuthenticationException();
        }

    }

    @Transactional
    public void updateExpertBySuperAdmin(ExpertUpdateRequestBySuperAdmin request) throws FirebaseAuthenticationException{
        // 1) Users 프록시 로드 (SELECT 없이)
        Users user = userRepository.getReferenceById(request.getId());
        // 2) 수정 허용 필드만 반영
        user.setName(request.getName());
        user.setNickname(request.getNickname());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());

        // 3) Expert 프록시 로드
        Expert expert = expertRepository.getReferenceById(request.getId());
        // 4) Expert 필드 반영
        expert.setMajor(request.getMajor());
        expert.setCareer(request.getCareer());
        // (license, address, isApproved 등 다른 필드는 건드리지 않습니다)
        try{
            String proxyUUID = user.getUuid();
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                // 변경이 감지되면, 원하는 초기값으로 설정
                String DEFAULT_PW = "Charti@1234";  // 예: 초기 비밀번호 상수
                firebaseService.initFirebasePassword(proxyUUID);
                user.setPassword(passwordEncoder.encode(DEFAULT_PW));
            }
        } catch (FirebaseAuthException firebaseAuthException){
            throw new FirebaseAuthenticationException();
        }

    }

    @Transactional
    public void updateManagerBySuperAdmin(ManagerUpdateRequestBySuperAdmin request) throws FirebaseAuthenticationException{
        // 1) Users 프록시만 로드
        Users user = userRepository.getReferenceById(request.getId());
        user.setName(request.getName());
        user.setNickname(request.getNickname());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());

        // 2) Manager 프록시 로드
        Manager manager = managerRepository.getReferenceById(request.getId());

        // 3) Group 프록시 로드
        Long groupId = manager.getGroup().getId();
        Group group = groupRepository.getReferenceById(groupId);
        group.setGroupName(request.getGroupName());
        group.setGroupEmail(request.getGroupEmail());

        try{
            String proxyUUID = user.getUuid();
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                // 변경이 감지되면, 원하는 초기값으로 설정
                String DEFAULT_PW = "Charti@1234";  // 예: 초기 비밀번호 상수
                firebaseService.initFirebasePassword(proxyUUID);
                user.setPassword(passwordEncoder.encode(DEFAULT_PW));
            }
        } catch (FirebaseAuthException firebaseAuthException){
            throw new FirebaseAuthenticationException();
        }

    }


    // [추가됨] 유저 검색용 메서드 - 포인트 지급 등의 기능에서 이름이나 닉네임으로 사용자 검색 시 사용
    public List<UserDTO> searchUsers(String keyword, String filter) {
        log.info("🔍 유저 검색 요청: filter={}, keyword={}", filter, keyword);
        List<Users> users;

        // 필터 조건 분기 처리
        switch (filter) {
            case "name":
                // 이름 기준 검색 (대소문자 구분 없이)
                users = userRepository.findByNameContainingIgnoreCase(keyword);
                break;
            case "nickname":
                // 닉네임 기준 검색 (대소문자 구분 없이)
                users = userRepository.findByNicknameContainingIgnoreCase(keyword);
                break;
            default:
                // 잘못된 필터 값일 경우 예외 처리
                throw new IllegalArgumentException("지원하지 않는 필터입니다: " + filter);
        }

        // 검색된 Users 리스트를 UserDTO로 변환하여 반환
        return users.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    // userID로 닉네임 조회
    @Transactional(readOnly = true)
    public String getNicknameByUsersId(Long usersId) {
        Member m = getMemberEntityById(usersId);
        // Member ↔ Users 연관관계가 있다면
        return m.getUsers().getNickname();
    }



    // Users → UserDTO → Member 로 안전하게 변환되므로 타입 충돌 없이 Member 엔티티 기반 기능(예: 문진, 자녀 조회 등)에 활용
    public Member getMemberEntityById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException());
    }

    public Users findByUsername(String username) {
        Optional<Users> user = userRepository.findByUsername(username);
        if(user.isPresent()){
            return user.get();
        } else{
            throw new UserNotFoundException("유저가 없어요");
        }
    }

    // 1) username(email) 으로 Users 엔티티 조회
    public Users findByUsernameEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("유저가 없어요: " + username));
    }

    // 2) uuid 로 Users 엔티티 조회 (이미 DTO용 getMemberByUUID 가 있지만, 엔티티가 필요하면)
    public Users findByUuidEntity(String uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("유저가 없어요 (uuid): " + uuid));
    }

    /**
     * 담당자 정보 업데이트 (Firebase + DB)
     * @param req 수정 요청 DTO
     * @param uid 인증된 사용자 UID
     * @throws FirebaseAuthException Firebase 업데이트 실패 시
     */
    @Transactional
    public void updateMember(UserUpdateRequest req, String uid) throws FirebaseAuthException {
        // Firebase 임시계정 삭제
        if (StringUtils.hasText(req.getNewUid())) {
            try {
                FirebaseAuth.getInstance().deleteUser(req.getNewUid());
            } catch (FirebaseAuthException e) {
                log.warn("임시 UID({}) 삭제 실패: {}", req.getNewUid(), e.getMessage());
            }
        }
        // 1. Firebase 동기화
        UserRecord.UpdateRequest firebaseReq = new UserRecord.UpdateRequest(uid);
        if (StringUtils.hasText(req.getName())) firebaseReq.setDisplayName(req.getName());
        if (StringUtils.hasText(req.getPhoneNumber())) firebaseReq.setPhoneNumber(req.getPhoneNumber());
        FirebaseAuth.getInstance().updateUser(firebaseReq);

        // 2. DB 동기화
        Users user = userRepository.findByUuid(uid)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 UID: " + uid));

        // 값이 있는 것만 수정
        updateIfPresent(req.getName(), user::setName);
        updateIfPresent(req.getNickname(), user::setNickname);
        updateIfPresent(req.getPhoneNumber(), user::setPhoneNumber);

        // Member 엔티티(주소 등)
        Member member = memberRepository.findWithUsersById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Member 정보 없음: " + user.getId()));

        // 주소 업데이트 (필요 시)
        if (req.getAddressId() != null) {
            Address addr = addressRepository.findById(req.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 주소 ID: " + req.getAddressId()));
            member.setAddress(addr);
        }

        // 저장
        memberRepository.save(member);
        userRepository.save(user);
    }

    // 람다 도우미는 그대로 사용 가능!
    private void updateIfPresent(String newValue, Consumer<String> setter) {
        if (StringUtils.hasText(newValue)) setter.accept(newValue);
    }

    /**
     * 사용자가 입력한 정보로 파이어베이스와 db의 패스워드를 수정한다
     *
     * @param currentPassword : 사용자가 입력한 현재 패스워드
     * @param newPassword : 사용자가 입력한 새 패스워드
     * @param confirmPassword : 사용자가 입력한 확인용 패스워드
     * @param uid : 접속중인 사용자 uid
     * @throws FirebaseAuthException 파이어베이스  예외
     */
    @Transactional
    public void changePassword(String uid, String currentPassword, String newPassword, String confirmPassword) throws FirebaseAuthException {
        Users user = userRepository.findByUuid(uid)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 기존 비밀번호와 새 비밀번호가 동일한지 확인
        else if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.");
        }

        // 새 비밀번호 확인
       else if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        // 🔥 Firebase 비밀번호 변경
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getUuid())
                .setPassword(newPassword);
        FirebaseAuth.getInstance().updateUser(request);


        // 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    @Transactional
    public void resetPassword(String username, String phoneNumber, String rawPassword) throws FirebaseAuthException {
        Users user = userRepository.findByUsernameAndPhoneNumber(username, phoneNumber)
                .orElseThrow(() -> new NoSuchElementException("사용자 없음"));

        log.info("🔐 변경 전 비번: {}", user.getPassword());

        // 1. Firebase 비밀번호 변경
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getUuid())
                .setPassword(rawPassword); // rawPassword 그대로 전달
        FirebaseAuth.getInstance().updateUser(request);

        // 2. DB 비밀번호도 변경 (암호화)
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        log.info("✅ 비밀번호 변경 완료 - Firebase + DB");
    }

    @Transactional
    public void softDeleteUser(String uid) {
        Users user = userRepository.findByUuid(uid)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다"));

        // Soft delete 처리
        user.markAsDeleted();
        userRepository.save(user);

        // ❗ Firebase 계정은 삭제하지 않고 유지 (선택적)
    }

    public PagingResultDTO<UserDTO,Users> searchDeletedUsers(String type, String keyword,Pageable pageable){
        Page<UserDTO> result = userQueryRepository.searchDeletedUsers(type,keyword,pageable);
        return new PagingResultDTO<>(result);
    }
    public PagingResultDTO<UserDTO,Users> getDeletedUserList(Pageable pageable){
        Page<UserDTO> result = userQueryRepository.getDeletedUserList(pageable);
        return new PagingResultDTO<>(result);
    }
    public Long getUserIdByUsername(String username){
        return userRepository.getIdByUsername(username);
    }
    public UserDTO getDeletedUserDTOById(Long userId) throws UserNotFoundException{
        Optional<UserDTO> dtoById = userQueryRepository.getDeletedUserDTOById(userId);

        if(dtoById.isEmpty()){
            throw new UserNotFoundException();
        }

        return dtoById.get();
    }

    public void restoreUsersById(Long id) throws UserNotFoundException,UserIsNotDeletedException{
        Optional<Boolean> isDeletedOpt = userRepository.checkIsDeletedById(id);

        if (isDeletedOpt.isEmpty()){
            throw new UserNotFoundException();
        } else{
            log.info("restoreUsersById Else Start");
            Boolean isDeleted = isDeletedOpt.get();
            log.info("restoreUsersById isDeletedOpt get execute");
            if (isDeleted){
                log.info("restoreUsersById isDeletedOpt true");
                userRepository.restoreUserById(id);
            } else{
                log.info("restoreUsersById isDeletedOpt false");
                throw new UserIsNotDeletedException();
            }

        }


    }

    /**
     * 주어진 userId 로 회원을 찾고,
     * 닉네임을 반환합니다. 존재하지 않으면 "손님"으로 대체.
     */
    public String findNicknameOrDefault(Long userId) {
        if (userId == null) {
            return "손님";
        }
        return userRepository.findById(userId)
                .map(Users::getNickname)
                .orElse("손님");
    }
}
