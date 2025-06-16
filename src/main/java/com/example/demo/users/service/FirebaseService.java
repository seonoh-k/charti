package com.example.demo.users.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.info.CommonInfo;
import com.example.demo.dto.request.MemberJoinRequest;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseService {

    private final FirebaseAuth firebaseAuth;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * 정상적으로 서명된 경우 이 메서드는 디코딩된 ID 토큰을 반환한다.
     *
     * @param idToken : 파이어베이스로부터 전달 받은 토큰을 의미한다.
     * @return FirebaseToken : 인증된 파이어베이스 토큰을 반환한다.<br/>내부에 claims가 정의되어 있으며 파이어베이스에 저장된 값을 확인할 수 있다.
     * @throws FirebaseAuthException 토큰을 검증하는동안 오류가 발생하거나 사용자가 disable인 경우.
     * @see <a href="https://firebase.google.com/docs/reference/admin/java/reference/com/google/firebase/auth/AbstractFirebaseAuth?_gl=1*1t1k9jm*_up*MQ..*_ga*Nzc2MjQ2MzAxLjE3NDk2MDU2NzA.*_ga_CW55HF8NVT*czE3NDk2MDU2NzAkbzEkZzAkdDE3NDk2MDU4MjkkajYwJGwwJGgw">Firebase SDK Document</a>
     */
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        return firebaseAuth.verifyIdToken(idToken);
    }

    /**
     * 이메일 로그인 유저의 계정을 파이어베이스에 생성한다.
     * <p>
     *  <ul>
     *      <li> 파이어베이스에서 계정을 생성한다.</li>
     *      <li> 계정을 생성하면 uuid를 반환하게 되고 이 값을 저장한 UserDTO를 반환한다.</li>
     *  </ul>
     * </p>
     * @param memberJoinRequest : 유저 정보를 담는 UserDTO
     * @return UserDTO : UUID가 추가된 UserDTO
     * @throws FirebaseAuthException - 계정을 생성하는 동안 에러가 발생하면 반환
     * @see <a href="https://firebase.google.com/docs/auth/admin/manage-users?hl=ko&_gl=1*1x2s6p8*_up*MQ..*_ga*MTczMzAzNTU3My4xNzQ5NTYyODkw*_ga_CW55HF8NVT*czE3NDk1NjI4ODkkbzEkZzAkdDE3NDk1NjI4ODkkajYwJGwwJGgw#java">Firebase SDK Document</a>
     */
    public MemberJoinRequest createMember(MemberJoinRequest memberJoinRequest) throws FirebaseAuthException {
        CommonInfo commonInfo = memberJoinRequest.getCommonInfo();
        UserRecord userRecord = firebaseAuth.createUser(new UserRecord.CreateRequest()
                .setEmail(commonInfo.getUsername())
                .setPassword(commonInfo.getPassword())
                .setPhoneNumber(commonInfo.getPhoneNumber())
                .setDisplayName(commonInfo.getName()));

        commonInfo.setUuid(userRecord.getUid());

        return memberJoinRequest;
    }
    /**
     * 소셜 로그인 유저의 계정을 파이어베이스에 생성한다.
     * <p>
     *  <ul>
     *      <li> 파이어베이스에서 계정을 생성한다.</li>
     *      <li> 계정을 생성하면 uuid를 반환하게 되고 이 값을 저장한 UserDTO를 반환한다.</li>
     *  </ul>
     * </p>
     * @param userDTO : 유저 정보를 담는 UserDTO
     * @return UserDTO : UUID가 추가된 UserDTO
     * @throws FirebaseAuthException - 계정을 생성하는 동안 에러가 발생하면 반환
     * @see <a href="https://firebase.google.com/docs/auth/admin/manage-users?hl=ko&_gl=1*1x2s6p8*_up*MQ..*_ga*MTczMzAzNTU3My4xNzQ5NTYyODkw*_ga_CW55HF8NVT*czE3NDk1NjI4ODkkbzEkZzAkdDE3NDk1NjI4ODkkajYwJGwwJGgw#java">Firebase SDK Document</a>
     */
    public UserDTO createSocialMember(UserDTO userDTO) throws FirebaseAuthException {

        UserRecord userRecord = firebaseAuth.createUser(new UserRecord.CreateRequest()
                .setEmail(userDTO.getUsername())
                .setDisplayName(userDTO.getName()));

        userDTO.setUuid(userRecord.getUid());

        return userDTO;
    }

    /**
     * 파이어베이스 계정의 권한을 Member로 입력한다.
     * <p>
     * 파이어베이스는 인가 기능을 제공하지 않아서 커스텀 Claims에 담아서 공유하는 방식으로 인가를 설정한다.
     *  <ul>
     *      <li> 데이터베이스에 uuid가 존재하는지 확인한다.</li>
     *      <li> 데이터베이스에 있는 권한 정보를 가져와서 claim에 담아 저장한다.</li>
     *  </ul>
     * </p>
     * @param userDTO : 유저 정보를 담는 dto
     * @return UserDTO
     * @throws FirebaseAuthException
     * @see <a href="https://firebase.google.com/docs/auth/admin/custom-claims?hl=ko#java">Firebase SDK Document</a>
     */
    public UserDTO setFirebaseMemberRoleToMember(UserDTO userDTO) throws FirebaseAuthException{

        if(!userRepository.existsByUuid(userDTO.getUuid())){
            throw new UserNotFoundException();
        }
        Optional<Users> byUuid = userRepository.findByUuid(userDTO.getUuid());

        String uuid = userDTO.getUuid();
        String role = byUuid.get().getRole().name();

        Map<String,Object> claims = new HashMap<>();
        claims.put("role", role);

        firebaseAuth.setCustomUserClaims(userDTO.getUuid(), claims);

        return userDTO;
    }
    /**
     * 파이어베이스 계정의 권한을 Member로 입력한다.
     * <p>
     * 파이어베이스는 인가 기능을 제공하지 않아서 커스텀 Claims에 담아서 공유하는 방식으로 인가를 설정한다.
     *  <ul>
     *      <li> 데이터베이스에 uuid가 존재하는지 확인한다.</li>
     *      <li> 데이터베이스에 있는 권한 정보를 가져와서 claim에 담아 저장한다.</li>
     *  </ul>
     * </p>
     *
     * @param memberJoinRequest : 유저 정보를 담는 dto
     * @throws FirebaseAuthException
     * @see <a href="https://firebase.google.com/docs/auth/admin/custom-claims?hl=ko#java">Firebase SDK Document</a>
     */
    public void setFirebaseMemberRoleToMember(MemberJoinRequest memberJoinRequest) throws FirebaseAuthException{
        CommonInfo commonInfo = memberJoinRequest.getCommonInfo();  //이렇게 선언하며 정보 잡을수 있나
        if(!userRepository.existsByUuid(commonInfo.getUuid())){
            throw new UserNotFoundException();
        }
        Optional<Users> byUuid = userRepository.findByUuid(commonInfo.getUuid());

        String uuid = commonInfo.getUuid();
        String role = byUuid.get().getRole().name();

        Map<String,Object> claims = new HashMap<>();
        claims.put("role", role);

        firebaseAuth.setCustomUserClaims(commonInfo.getUuid(), claims);

    }
    /**
     * 파이어베이스 토큰을 간접적으로 정지한다.
     * <p>
     * <div>ID 토큰 : 본래 만료 시간 까지는 사용 가능</div>
     * <div>리프레시 토큰 : 즉시 정지</div>
     * </p>
     * @param uuid : 파이어베이스 유저 식별자
     * @throws FirebaseAuthException
     * @see <a href="https://firebase.google.com/docs/auth/admin/manage-sessions?hl=ko&_gl=1*7zw0hp*_up*MQ..*_ga*MTczMzAzNTU3My4xNzQ5NTYyODkw*_ga_CW55HF8NVT*czE3NDk1NjI4ODkkbzEkZzAkdDE3NDk1NjI4ODkkajYwJGwwJGgw#java">Firebase SDK Document</a>
     */
    public void refreshToken(String uuid) throws FirebaseAuthException{
        firebaseAuth.revokeRefreshTokens(uuid);
    }

    /**
     * 파이어베이스에 계정이 있는지 이메일로 검사한다.
     *
     * @param email : 검사할 이메일
     * @Return true : 계정 생성 불가 , false : 계정 생성 가능
     * @throws FirebaseAuthException
     * @see <a href="https://firebase.google.com/docs/auth/admin/manage-users?hl=ko&_gl=1*1gg210o*_up*MQ..*_ga*MTczMzAzNTU3My4xNzQ5NTYyODkw*_ga_CW55HF8NVT*czE3NDk1NjI4ODkkbzEkZzAkdDE3NDk1NjI4ODkkajYwJGwwJGgw#java">Firebase SDK Document</a>
     */
    public boolean existsByByEmail(String email){
        try{
            UserRecord byEmail = firebaseAuth.getUserByEmail(email);
            return true;
        } catch (FirebaseAuthException e){
            log.info("👀 FirebaseService.checkDuplicateByEmail Email Is Not Duplicate");
            return false;
        }
    }

    /**
     *
     * @param uuid : Firebase에서 생성한 uuid 값 유저의 식별자로 사용 가능하다.
     * @return
     * @throws FirebaseAuthException
     * @see <a href="https://firebase.google.com/docs/auth/admin/manage-users?hl=ko&_gl=1*1gg210o*_up*MQ..*_ga*MTczMzAzNTU3My4xNzQ5NTYyODkw*_ga_CW55HF8NVT*czE3NDk1NjI4ODkkbzEkZzAkdDE3NDk1NjI4ODkkajYwJGwwJGgw#java">Firebase SDK Document</a>
     */
    public String createFirebaseCustomToken(String uuid) throws FirebaseAuthException{
        return firebaseAuth.createCustomToken(uuid);
    }
    /**
     * 주어진 UID에 대해 지정된 추가 클레임을 포함하는 Firebase 커스텀 토큰을 생성
     * <br/>
     *
     * 이 토큰은 클라이언트 페이지로 전송되어 signInWithCustomToken 인증 API와 함께 사용하여 소셜 로그인 인증을 진행하게된다.</br>
     * 다음과 같은 프로세스로 파이어베이스 토큰 생성을 진행한다.(참고만)
     *<ol>
     *     <li>초기화 시 제공된 경우 FirebaseApp의 서비스 계정 자격 증명의 개인 키(private key)를 사용</li>
     *     <li>서비스 계정 이메일이 지정된 경우 IAM 서비스를 사용</li>
     *     <li>코드가 Google App Engine 표준 환경에 배포된 경우 App Identity 서비스를 사용</li>
     *     <li>코드가 Google Compute Engine과 같은 다른 GCP 관리 환경에 배포된 경우 로컬 Metadata 서버를 사용</li>
     *</ol>
     * @param uuid Firebase에서 생성한 uuid 값 유저의 식별자로 사용 가능하다.
     * @param claims 토큰에 저장될 추가 클레임(데이터베이스, 스토리지 등의 보안 규칙에서 사용 가능)<br/>
     *               JSON으로 직렬화 가능해야한다.(예: Map, Array, String, Boolean, Number 등만 포함).
     * @return String Firebase 커스텀 토큰 문자열.
     * @throws FirebaseAuthException - 커스텀 토큰 생성 중 오류가 발생할 경우.
     * @throws IllegalArgumentException – 지정된 uid가 null이거나 비어 있을 경우.
     * @throws IllegalStateException – SDK가 토큰 서명을 위한 유효한 방법을 찾지 못할 경우.
     * @see <a href="https://firebase.google.com/docs/auth/admin/create-custom-tokens?hl=ko&_gl=1*1ob7req*_up*MQ..*_ga*NzQ4MjAyMDc0LjE3NDk2MDc2NTE.*_ga_CW55HF8NVT*czE3NDk2MDc2NTAkbzEkZzAkdDE3NDk2MDc2NTAkajYwJGwwJGgw#java">Firebase SDK Document</a>
     */
    public String createFirebaseCustomToken(String uuid, Map<String, Object> claims) throws FirebaseAuthException{
        return firebaseAuth.createCustomToken(uuid, claims);
    }
    public void deleteFirebaseMember(String uuid) throws FirebaseAuthException{
        firebaseAuth.deleteUser(uuid);
    }

    /**
     * uuid로 가져온 사용자들을 삭제한다.
     * <br/><br/>
     * 존재하지 않는 사용자를 삭제해도 오류가 발생하지 않는다.<br/>
     * 존재하지 않는 사용자도 성공적으로 삭제된 것으로 간주되어 <br/>{@link DeleteUsersResult.getSuccessCount()}에 포함된다.<br/>
     * 최대 1000개의 식별자만 전달할 수 있으며, 1000개를 초과하는 식별자를 전달하면 {@link IllegalArgumentException}이 발생한ㄷ.<br/>
     * 1000명 이상을 일괄 삭제하려면 호출 간에 지연을 추가하여 속도 제한에 걸리지 않도록 하세요.
     * @param uuidList 삭제할 사용자 UUID List ->  최대 1000개까지 포함 가능하다.
     * @return      A {@link DeleteUsersResult} 성공 및 실패한 삭제 수와 실패한 항목에 대한 세부 정보를 담은 {@link DeleteUsersResult}를 반환합니다.
     * @throws     IllegalArgumentException 식별자가 null이거나 비어 있거나 1000개를 초과하여 전달된 경우 발생합니다.
     * @throws     FirebaseAuthException 사용자 삭제 중 Firebase 측에서 오류가 발생하면 발생합니다.
     */
    public void deleteFirebaseMemberList(List<String> uuidList) throws FirebaseAuthException{
        firebaseAuth.deleteUsers(uuidList);
    }






}
