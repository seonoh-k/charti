package com.example.demo.users.service;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.ExpertJoinRequest;
import com.example.demo.dto.request.ManagerJoinRequest;
import com.example.demo.dto.request.MemberJoinRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.users.entity.*;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.users.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;
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
    private final PasswordEncoder passwordEncoder;


    public UserDTO entityToDTO(Users users) {

        if (users == null) return null;

        return UserDTO.builder()
                .id(users.getId())
                .username(users.getUsername())
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

            userRepository.save(this.dtoToEntity(userDTO));


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

    // Member
    public PagingResultDTO<MemberDTO, Users> getMemberListWithPaging(Pageable pageable) {
        Page<Users> result = userRepository.getAllMember(pageable);
        result.forEach(users -> log.info("ID:" + users.getId()));
        return new PagingResultDTO<>(result, MemberDTO::fromEntity);
    }
    public PagingResultDTO<MemberDTO, Users> getMemberListWithPaging(String type, String keyword, Pageable pageable) {
        Page<Users> result = userRepository.getAllMember(type,keyword,pageable);
        return new PagingResultDTO<>(result, MemberDTO::fromEntity);
    }

    public List<MemberDTO> getMemberList(Pageable pageable){

        Page<Users> allMember = userRepository.getAllMember(pageable);
        // Entity -> DTO로 변환 DTO 클래스의 Static Method 사용
        List<MemberDTO> list = allMember.map(MemberDTO::fromEntity).toList();
        return list;
    }
    public List<ExpertDTO>  getUnApprovedExpertList(Pageable pageable){
        Page<Users> allExpertUnApproved = userRepository.getAllExpertUnApproved(pageable);
        // Entity -> DTO의 Static Method 사용해서 변환
        List<ExpertDTO> list = allExpertUnApproved .map(ExpertDTO::fromEntity).toList();
        return list;
    }
    public List<ManagerDTO>  getUnApprovedManagerList(Pageable pageable){
        Page<Users> allManagerUnApproved = userRepository.getAllManagerUnApproved(pageable);
        // Entity -> DTO의 Static Method 사용해서 변환
        List<ManagerDTO> list = allManagerUnApproved.map(ManagerDTO::fromEntity).toList();
        return list;
    }
    public PagingResultDTO<ManagerDTO, Users>  getApprovedManagerListWithPaging(Pageable pageable){
        Page<Users> result = userRepository.getAllManagerApproved(pageable);
        // Entity -> DTO의 Static Method 사용해서 변환

        return new PagingResultDTO<>(result, ManagerDTO::fromEntity);
    }
    public PagingResultDTO<ExpertDTO, Users>  getApprovedExpertListWithPaging(Pageable pageable){
        Page<Users> result = userRepository.getAllExpertApproved(pageable);
        // Entity -> DTO의 Static Method 사용해서 변환
        return new PagingResultDTO<>(result, ExpertDTO::fromEntity);
    }
    public UserStatus updateMember(UserDTO userDTO){
        Long id = userDTO.getId();
        Optional<Users> byId = userRepository.findById(id);
        if (byId.isPresent()){
            Users users = byId.get();

            // JPA는 영속 상태 엔티티의 필드가 변경되면 자동으로 update 쿼리 발생
            // 엔티티를 가져와서 Set 하면 DirtyChecking사용한 update
            // 그렇다면 Member를 변경하려면 어떻게 해야할까?
            // Member에서 관리자가 변경 가능한 항목은 ?
            // 어떤 항목이 비즈니스 관점에서 변경 가능 해야 하고 변경 불가 한가?
            // 관리자 일반 회원들 확인 하는 목록에서는 어떤 정보를 확인할 수 있어야 하는지?
            // 이름,닉네임, 아이디(이메일), 전화번호,가입일,가족,포인트, 소셜로그인 사용 여부
            return UserStatus.UPDATE_SUCCESS;
        } else{
            throw new UserNotFoundException();
        }
    }
    public UserStatus updateExpert(ExpertDTO expertDTO){
        Long id = expertDTO.getId();
        Optional<Users> byId = userRepository.findById(id);
        if (byId.isPresent()){
            Users users = byId.get();

            // JPA는 영속 상태 엔티티의 필드가 변경되면 자동으로 update 쿼리 발생
            // 엔티티를 가져와서 Set 하면 DirtyChecking사용한 update
            // 그렇다면 Member를 변경하려면 어떻게 해야할까?
            // Member에서 관리자가 변경 가능한 항목은 ?
            // 어떤 항목이 비즈니스 관점에서 변경 가능 해야 하고 변경 불가 한가?
            // 관리자 일반 회원들 확인 하는 목록에서는 어떤 정보를 확인할 수 있어야 하는지?
            // 이름,닉네임, 아이디(이메일), 전화번호,가입일,가족,포인트, 소셜로그인 사용 여부
            return UserStatus.UPDATE_SUCCESS;
        } else{
            throw new UserNotFoundException();
        }
    }
    public UserStatus updateManager(ManagerDTO managerDTO){
        Long id = managerDTO.getId();
        Optional<Users> byId = userRepository.findById(id);
        if (byId.isPresent()){
            Users users = byId.get();

            // JPA는 영속 상태 엔티티의 필드가 변경되면 자동으로 update 쿼리 발생
            // 엔티티를 가져와서 Set 하면 DirtyChecking사용한 update
            // 그렇다면 Member를 변경하려면 어떻게 해야할까?
            // Member에서 관리자가 변경 가능한 항목은 ?
            // 어떤 항목이 비즈니스 관점에서 변경 가능 해야 하고 변경 불가 한가?
            // 관리자 일반 회원들 확인 하는 목록에서는 어떤 정보를 확인할 수 있어야 하는지?
            // 이름,닉네임, 아이디(이메일), 전화번호,가입일,가족,포인트, 소셜로그인 사용 여부
            return UserStatus.UPDATE_SUCCESS;
        } else{
            throw new UserNotFoundException();
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

    // 미승인된 전문가 검색
    public PagingResultDTO<ExpertDTO, Users> getPendingExpertListWithPaging(String type,String keyword,Pageable pageable) {
        Page<Users> result = userRepository.searchExpertUnApprovedWithKeyword(type,keyword,pageable);
        return new PagingResultDTO<>(result, ExpertDTO::fromEntity);
    }

    public PagingResultDTO<ExpertDTO, Users> getPendingExpertListWithPaging(Pageable pageable) {

        Page<Users> result = userRepository.getAllExpertUnApproved(pageable);
        return new PagingResultDTO<>(result, ExpertDTO::fromEntity);
    }
    // 미승인된 담당자 검색 에 사용
    public PagingResultDTO<ManagerDTO, Users> getPendingManagerListWithPaging(String type,String keyword,Pageable pageable) {
        Page<Users> result = userRepository.searchManagerUnApprovedWithKeyword(type,keyword,pageable);
        return new PagingResultDTO<>(result, ManagerDTO::fromEntity);
    }
    // 미승인된 담당자 검색 에 사용
    public PagingResultDTO<ManagerDTO, Users> getPendingManagerListWithPaging(Pageable pageable) {

        Page<Users> result = userRepository.getAllManagerUnApproved(pageable);
        return new PagingResultDTO<>(result, ManagerDTO::fromEntity);
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

    public MemberDTO findMemberById(Long id) throws UserNotFoundException{
        Optional<Users> byId = userRepository.findById(id);
        if (byId.isEmpty()){
            throw new UserNotFoundException();
        }
        MemberDTO memberDTO = MemberDTO.fromEntity(byId.get());
        return memberDTO;
    }
    public ExpertDTO findExpertById(Long id) throws UserNotFoundException{
        Optional<Users> byId = userRepository.findById(id);
        if (byId.isEmpty()){
            throw new UserNotFoundException();
        }
        ExpertDTO expertDTO = ExpertDTO.fromEntity(byId.get());
        return expertDTO;
    }
    public ManagerDTO findManagerById(Long id) throws UserNotFoundException {
        Optional<Users> byId = userRepository.findById(id);
        if (byId.isEmpty()) {
            throw new UserNotFoundException();
        }
        ManagerDTO managerDTO = ManagerDTO.fromEntity(byId.get());
        return managerDTO;
    }
  
    /**
     * UUID로 유저를 가져온다.
     *
     * @param request : 사용자가 입력한 수정 데이터
     * @param uid : 접속중인 사용자 uid
     * @throws FirebaseAuthException 파이어베이스  예외
     */
    @Transactional
    public void updateUser(UserUpdateRequest request, String uid) throws FirebaseAuthException {
        // 1. Firebase 업데이트
        UserRecord.UpdateRequest firebaseReq = new UserRecord.UpdateRequest(uid)
                .setDisplayName(request.getName())
                .setPhoneNumber(request.getPhoneNumber());

        FirebaseAuth.getInstance().updateUser(firebaseReq);

        // 2. DB 업데이트
        Users user = userRepository.findByUuid(uid).orElseThrow();
        user.setName(request.getName());
        user.setNickname(request.getNickname());

//        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * UUID로 유저를 가져온다.
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
    public void softDeleteUser(String uid) {
        Users user = userRepository.findByUuid(uid)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다"));

        // Soft delete 처리
        user.markAsDeleted();
        userRepository.save(user);

        // ❗ Firebase 계정은 삭제하지 않고 유지 (선택적)
    }


}
