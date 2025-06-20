package com.example.demo.users.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.info.AddressInfo;
import com.example.demo.dto.info.CommonInfo;
import com.example.demo.dto.request.ExpertJoinRequest;
import com.example.demo.dto.request.ManagerJoinRequest;
import com.example.demo.dto.request.MemberJoinRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Address;
import com.example.demo.entity.Group;
import com.example.demo.exception.JwtTokenFormatInvalidException;
import com.example.demo.exception.JwtTokenNotFoundException;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.Role;
import com.example.demo.users.exception.UserAlreadyExistsException;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.FirebaseService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.GlobalStatus;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 회원가입/로그인/인가(Authorization) 및 인증(Authentication) 처리를 담당
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final FirebaseService firebaseService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final AddressRepository addressRepository;  // 주소 검색 요청을위해 임시사용
    private final AddressService addressService;  // 그룹 주소 검색 을 위해 임시사용
    private final GroupRepository groupRepository;  // 그룹 검색 을 위해 임시사용
    private final ManagerRepository managerRepository;  // 그룹중복 검사를  위해 임시사용
    private final UserRepository userRepository;  // 그룹중복 검사를  위해 임시사용

    /**
     * 로그인 Users 테이블에 있는 데이터로 검사를 진행한다.
     *
     * @param authHeader
     * @return ApiResponse StatusCode : ["AF","AS"]
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT TOKEN FORMAT => "Bearer TokenValueIsRandomTextAndIncludingNumber"
            String idToken = jwtUtil.removeBearerPrefix(authHeader);
            FirebaseToken decoded = firebaseService.verifyIdToken(idToken);
            String email = decoded.getEmail();

            userService.getMemberByEmail(email);

            String jwt = jwtUtil.createToken(decoded);
            ResponseCookie jwtCookie = jwtUtil.createCookie(jwt);

            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(new ApiResponse(AuthStatus.AUTHENTICATION_SUCCESS));

        } catch (IllegalStateException e) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse(AuthStatus.USER_DELETED));

        }catch (JwtTokenNotFoundException jwtTokenNotFoundException){

            jwtTokenNotFoundException.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(AuthStatus.TOKEN_NOT_FOUND));

        } catch (JwtTokenFormatInvalidException jwtTokenFormatInvalidException){

            jwtTokenFormatInvalidException.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(AuthStatus.TOKEN_INVALID_FORMAT));

        } catch (FirebaseAuthException firebaseAuthException) {

            log.info("⚠️ [AuthController.loginMember] FirebaseAuthException : {}",
                    firebaseAuthException.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(AuthStatus.AUTHENTICATION_FAIL));

        } catch (UserNotFoundException userNotFoundException){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(AuthStatus.USER_NOT_REGISTRATION));

        }
    }
    @GetMapping("/findUsername")
    public String showFindUsernamePage() {
        return "findUsername"; // 템플릿 경로 (예: /templates/user/find-username.html)
    }


    /**
     * 사용자가 입력한 동 이름으로 주소 목록을 검색해 반환한다.
     *
     * @param dong 사용자가 입력한 동 이름 (최소 2글자 이상)
     * @return 동 이름이 포함된 주소들의 리스트 (Map 형태)
     */
    @ResponseBody
    @GetMapping("/api/address/search")      //  address 관련으로 컨트롤러 가를것 같음
    public List<Map<String, Object>> searchByDong(@RequestParam String dong) {
        if (dong.length() < 2) {
            //  빈 리스트 반환, 상황에 따라 에러 응답도 가능
            return List.of();
        }

        List<Address> addressList = addressRepository.findSimpleByDong(dong);

        // 헤딩 작업코드는 추후 서비스로 변경 예정
        return addressList.stream().map(address -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", address.getId());
            map.put("zipNum", address.getZipNum());
            map.put("sido", address.getSido());
            map.put("gugun", address.getGugun());
            map.put("dong", address.getDong());
            // ✅ bunji가 null도 아니고 빈 문자열도 아닐 때만 넣기
            if (address.getBunji() != null && !address.getBunji().isBlank()) {
                map.put("bunji", address.getBunji());
            }
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 입력된 휴대전화 번호의 존재 여부를 확인한다.
     *
     * @param req 요청 본문에 포함된 전화번호 (예: {"phoneNumber": "01012345678"})
     * @return {"exists": true} 또는 {"exists": false}
     */
    @PostMapping(value ="/api/check-phone")
    @ResponseBody
    public Map<String, Boolean> checkPhone(@RequestBody Map<String, String> req) {

        String phoneNumber = req.get("phoneNumber");
        log.info("👋[AuthController.checkPhone]  phoneNumber : {}", phoneNumber);
        boolean exists = userService.existsByPhoneNumber(phoneNumber);
        return Map.of("exists", exists);

    }

    /**
     * 전화번호를 인증 후 SmsIdToken을 발급 받고 해당 토큰으로 인증을 받아 회원가입을 진행한다.
     *
     * @param memberJoinRequest : 폼 양식으로 받은 DTO 객체
     * @return
     */
@PostMapping("/join/member")                                            // 기본정보 + 추가정보 + 주소정보
    public ResponseEntity<ApiResponse> joinMember(@Valid @RequestBody MemberJoinRequest memberJoinRequest, BindingResult bindingResult) {
        // 1. 유효성 검사 실패하면 파이어베이스 계정 삭제  - 미구현
        try {
            // 화원가입 유효성 검사
            if (bindingResult.hasErrors()) {

                log.info("[POST] 🎈 : 유효성 검사 걸림");
                FieldError fieldError = bindingResult.getFieldError();  // 검증에 실패한 필드의 정보
                String errorMsg =  fieldError.getDefaultMessage();     //오류메세지  비밀번호는 필수 입력값입니다.
                String fieldName = fieldError.getField();  // 예: "commonInfo.password"
                String pureFieldName = fieldName.contains(".")     // .을 기준으로 뒤쪽 값
                        ? fieldName.substring(fieldName.lastIndexOf('.') + 1)
                        : fieldName;
                log.info("[POST] 🎈 error on field '{}': {}", fieldName, errorMsg);
                log.info("[POST] 🎈 error on field '{}': {}", pureFieldName, errorMsg);

                // 여기에 CUSTOM 메시지를 넣어서 ApiResponse를 만듭니다.
                ApiResponse<String> errorResponse =
                        new ApiResponse<>(AuthStatus.VALIDATION_FAILED,pureFieldName,errorMsg);
                // 400 에러와 errorResponse 담아서 리턴
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorResponse);
            }
            log.info("[POST] 🎈 MemberJoinRequest '{}' :  ",memberJoinRequest);      // 정보 확인용 로그들
            log.info("[POST] 🎈 MemberJoinRequest '{}' :  ",memberJoinRequest.getCommonInfo());
            log.info("[POST] 🎈 MemberJoinRequest 이름: '{}' :  ",memberJoinRequest.getCommonInfo().getName());
            log.info("[POST] 🎈 MemberJoinRequest 닉네임 :'{}' :  ",memberJoinRequest.getCommonInfo().getNickname());
            log.info("[POST] 🎈 MemberJoinRequest 아이디 '{}' :  ",memberJoinRequest.getCommonInfo().getUsername());
            log.info("[POST] 🎈 MemberJoinRequest 비밀번호 '{}' :  ",memberJoinRequest.getCommonInfo().getPassword());
            log.info("[POST] 🎈 MemberJoinRequest id 토큰 '{}' :  ",memberJoinRequest.getCommonInfo().getSmsIdToken());
            log.info("[POST] 🎈 MemberJoinRequest 주소 아이디 '{}' :  ",memberJoinRequest.getAddressInfo().getAddressId());

            // 공통 정보 인포
            CommonInfo commonInfo = memberJoinRequest.getCommonInfo();

            FirebaseToken firebaseToken = firebaseService.verifyIdToken(commonInfo.getSmsIdToken());

            String uid = firebaseToken.getUid();
            firebaseService.deleteFirebaseMember(uid);
            String phoneNumber = (String) firebaseToken.getClaims().get("phone_number");
            log.info("[POST] 🎈 MemberJoinRequest 전화번호 '{}' :  ",phoneNumber);

            // 인증 실패: 잘못된 토큰이거나 SMS 인증이 아님
            if (phoneNumber == null) {
                // code : "PAF", message : "sms 인증 실패"
                log.info("[POST] 🎈 설마 여기서 걸렸나? : sms인증토큰이 이상한가  ");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(AuthStatus.PHONE_AUTH_FAIL));
            }

            commonInfo.setPhoneNumber(phoneNumber);
            commonInfo.setRole(Role.ROLE_MEMBER.name());
            log.info("[POST] 🎈 MemberJoinRequest 저장된전화번호 '{}' :  ", commonInfo.getPhoneNumber());
            log.info("[POST] 🎈 MemberJoinRequest 저장된 롤 '{}' :  ", commonInfo.getRole());

            CommonInfo savedUserInfo = firebaseService.createMember(commonInfo);    // FirebaseAuthException

            AuthStatus authStatus = authService.createMemberJoinRequest(memberJoinRequest,savedUserInfo);   // UserAlreadyExistsException

            firebaseService.setFirebaseMemberRoleToMember(savedUserInfo);   // UserNotFoundException

            // 일반회원 회원가입 성공 메세지
            if (authStatus == AuthStatus.MEMBER_JOIN_REQUEST_SUCCESS) {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(authStatus));

            } else {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(authStatus));

            }

        } catch (FirebaseAuthException firebaseAuthException) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(AuthStatus.SERVER_ERROR));

        } catch (IllegalArgumentException illegalArgumentException) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(AuthStatus.AUTHENTICATION_FAIL));

        }
    }

    @PostMapping("/join/expert")
    public ResponseEntity<ApiResponse> joinExpert(@Valid @RequestBody ExpertJoinRequest expertJoinRequest, BindingResult bindingResult){
        try{
            // 화원가입 유효성 검사
            if (bindingResult.hasErrors()) {

                log.info("[POST] 🎈 : 유효성 검사 걸림");
                FieldError fieldError = bindingResult.getFieldError();  // 검증에 실패한 필드의 정보
                String errorMsg =  fieldError.getDefaultMessage();     //오류메세지  비밀번호는 필수 입력값입니다.
                String fieldName = fieldError.getField();  // 예: "commonInfo.password"
                String pureFieldName = fieldName.contains(".")     // .을 기준으로 뒤쪽 값
                        ? fieldName.substring(fieldName.lastIndexOf('.') + 1)
                        : fieldName;
                log.info("[POST] 🎈 error on field '{}': {}", fieldName, errorMsg);
                log.info("[POST] 🎈 error on field '{}': {}", pureFieldName, errorMsg);

                // 여기에 CUSTOM 메시지를 넣어서 ApiResponse를 만듭니다.
                ApiResponse<String> errorResponse =
                        new ApiResponse<>(AuthStatus.VALIDATION_FAILED,pureFieldName,errorMsg);
                // 400 에러와 errorResponse 담아서 리턴
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorResponse);
            }
            log.info("[POST] 🎈 expertJoinRequest '{}' :  ",expertJoinRequest);      // 정보 확인용 로그들
            log.info("[POST] 🎈 expertJoinRequest '{}' :  ",expertJoinRequest.getCommonInfo());
            log.info("[POST] 🎈 expertJoinRequest 이름: '{}' :  ",expertJoinRequest.getCommonInfo().getName());
            log.info("[POST] 🎈 expertJoinRequest 닉네임 :'{}' :  ",expertJoinRequest.getCommonInfo().getNickname());
            log.info("[POST] 🎈 expertJoinRequest 아이디 '{}' :  ",expertJoinRequest.getCommonInfo().getUsername());
            log.info("[POST] 🎈 expertJoinRequest 비밀번호 '{}' :  ",expertJoinRequest.getCommonInfo().getPassword());
            log.info("[POST] 🎈 expertJoinRequest id 토큰 '{}' :  ",expertJoinRequest.getCommonInfo().getSmsIdToken());
            log.info("[POST] 🎈 expertJoinRequest 주소 아이디 '{}' :  ",expertJoinRequest.getAddressInfo().getAddressId());
            log.info("[POST] 🎈 expertJoinRequest 전문가인포에 주소가? '{}' :  ",expertJoinRequest.getExpertInfo().getAddressId());
            log.info("[POST] 🎈 expertJoinRequest 전공 '{}' :  ",expertJoinRequest.getExpertInfo().getMajor());
            log.info("[POST] 🎈 expertJoinRequest 경력사항 '{}' :  ",expertJoinRequest.getExpertInfo().getCareer());
            log.info("[POST] 🎈 expertJoinRequest 자격증파일 이름 '{}' :  ",expertJoinRequest.getExpertInfo().getLicense());

            // 공통 정보 인포
            CommonInfo commonInfo = expertJoinRequest.getCommonInfo();

            FirebaseToken firebaseToken = firebaseService.verifyIdToken(commonInfo.getSmsIdToken());

            String uid = firebaseToken.getUid();
            firebaseService.deleteFirebaseMember(uid);
            String phoneNumber = (String) firebaseToken.getClaims().get("phone_number");
            log.info("[POST] 🎈 expertJoinRequest 전화번호 '{}' :  ",phoneNumber);

            // 인증 실패: 잘못된 토큰이거나 SMS 인증이 아님
            if (phoneNumber == null) {
                // code : "PAF", message : "sms 인증 실패"
                log.info("[POST] 🎈 설마 여기서 걸렸나? : sms인증토큰이 이상한가  ");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(AuthStatus.PHONE_AUTH_FAIL));
            }
            commonInfo.setPhoneNumber(phoneNumber);
            commonInfo.setRole(Role.ROLE_MEMBER.name());
            log.info("[POST] 🎈 expertJoinRequest 저장된전화번호 '{}' :  ", commonInfo.getPhoneNumber());
            log.info("[POST] 🎈 expertJoinRequest 저장된 롤 '{}' :  ", commonInfo.getRole());

            CommonInfo savedUserDTO = firebaseService.createMember(commonInfo); // FirebaseAuthException

            AuthStatus authStatus = authService.createExpertJoinRequest(expertJoinRequest,savedUserDTO);    // UserAlreadyExistsException

            firebaseService.setFirebaseMemberRoleToMember(savedUserDTO);    // UserNotFoundException

            if (authStatus == AuthStatus.EXPERT_JOIN_REQUEST_SUCCESS) {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(authStatus));

            } else {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(authStatus));

            }


        } catch (FirebaseAuthException firebaseAuthException) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(AuthStatus.SERVER_ERROR));


        } catch (UserAlreadyExistsException userAlreadyExistsException){

            return ResponseEntity.status(HttpStatus.CONFLICT)
                     .body(new ApiResponse(AuthStatus.USER_DUPLICATE));

        }
    }


    @GetMapping("/api/address/{id}")
    public ResponseEntity<Map<String, Object>> getAddressById(@PathVariable Long id) {
        Address address = addressService.getAddressById(id); // 예외 포함
        Map<String, Object> map = new HashMap<>();
        map.put("id", address.getId());
        map.put("zipNum", address.getZipNum());
        map.put("sido", address.getSido());
        map.put("gugun", address.getGugun());
        map.put("dong", address.getDong());
        map.put("bunji", address.getBunji());
        return ResponseEntity.ok(map);
    }
    @ResponseBody   // 그룹 찾기 컨트롤러 나중에 다른곳으로 가를것
    @GetMapping("/api/group/search")
    public List<Map<String, Object>> searchGroupByName(@RequestParam String name) {
        // 검색어가 너무 짧으면 빈 리스트 반환
        if (name.length() < 2) {
            return List.of();
        }
        List<Group> groupList = groupRepository.findByGroupNameContainingIgnoreCase(name);
        return groupList.stream().map(group -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", group.getId());
            map.put("name", group.getGroupName());
            map.put("email", group.getGroupEmail());
            map.put("phoneNumber", group.getGroupPhoneNumber());
            map.put("addressId", group.getAddress().getId());
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/join/manager")
    public ResponseEntity<ApiResponse> joinManager(@Valid @RequestBody ManagerJoinRequest managerJoinRequest, BindingResult bindingResult){
        try{
            // 화원가입 유효성 검사
            if (bindingResult.hasErrors()) {

                log.info("[POST] 🎈 : 유효성 검사 걸림");
                FieldError fieldError = bindingResult.getFieldError();  // 검증에 실패한 필드의 정보
                String errorMsg =  fieldError.getDefaultMessage();     //오류메세지  비밀번호는 필수 입력값입니다.
                String fieldName = fieldError.getField();  // 예: "commonInfo.password"
                String pureFieldName = fieldName.contains(".")     // .을 기준으로 뒤쪽 값
                        ? fieldName.substring(fieldName.lastIndexOf('.') + 1)
                        : fieldName;
                log.info("[POST] 🎈 error on field '{}': {}", fieldName, errorMsg);
                log.info("[POST] 🎈 error on field '{}': {}", pureFieldName, errorMsg);

                // 여기에 CUSTOM 메시지를 넣어서 ApiResponse를 만듭니다.
                ApiResponse<String> errorResponse =
                        new ApiResponse<>(AuthStatus.VALIDATION_FAILED,pureFieldName,errorMsg);
                // 400 에러와 errorResponse 담아서 리턴
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorResponse);
            }
            log.info("[POST] 🎈 managerJoinRequest '{}' :  ",managerJoinRequest);      // 정보 확인용 로그들
            log.info("[POST] 🎈 managerJoinRequest '{}' :  ",managerJoinRequest.getCommonInfo());
            log.info("[POST] 🎈 managerJoinRequest 이름: '{}' :  ",managerJoinRequest.getCommonInfo().getName());
            log.info("[POST] 🎈 managerJoinRequest 닉네임 :'{}' :  ",managerJoinRequest.getCommonInfo().getNickname());
            log.info("[POST] 🎈 managerJoinRequest 아이디 '{}' :  ",managerJoinRequest.getCommonInfo().getUsername());
            log.info("[POST] 🎈 managerJoinRequest 비밀번호 '{}' :  ",managerJoinRequest.getCommonInfo().getPassword());
            log.info("[POST] 🎈 managerJoinRequest id 토큰 '{}' :  ",managerJoinRequest.getCommonInfo().getSmsIdToken());
            log.info("[POST] 🎈 managerJoinRequest 주소 id '{}' :  ",managerJoinRequest.getAddressInfo().getAddressId());
            log.info("[POST] 🎈 managerJoinRequest 담당자 포지션 '{}' :  ",managerJoinRequest.getManagerInfo().getPosition());
            log.info("[POST] 🎈 managerJoinRequest 그룹 이메일 '{}' :  ",managerJoinRequest.getGroupInfo().getGroupEmail());
            log.info("[POST] 🎈 managerJoinRequest 그룹 전화번호 '{}' :  ",managerJoinRequest.getGroupInfo().getGroupPhoneNumber());
            log.info("[POST] 🎈 managerJoinRequest 그룹 이름 '{}' :  ",managerJoinRequest.getGroupInfo().getGroupName());
            log.info("[POST] 🎈 managerJoinRequest 그룹 id '{}' :  ",managerJoinRequest.getGroupInfo().getGroupId());
            log.info("[POST] 🎈 managerJoinRequest 그룹 카테고리 '{}' :  ",managerJoinRequest.getGroupInfo().getCategory());


            // 공통 정보 인포
            CommonInfo commonInfo = managerJoinRequest.getCommonInfo();

            FirebaseToken firebaseToken = firebaseService.verifyIdToken(commonInfo.getSmsIdToken());

            String uid = firebaseToken.getUid();
            firebaseService.deleteFirebaseMember(uid);
            String phoneNumber = (String) firebaseToken.getClaims().get("phone_number");
            log.info("[POST] 🎈 expertJoinRequest 전화번호 '{}' :  ",phoneNumber);

            // 인증 실패: 잘못된 토큰이거나 SMS 인증이 아님
            if (phoneNumber == null) {
                // code : "PAF", message : "sms 인증 실패"
                log.info("[POST] 🎈 설마 여기서 걸렸나? : sms인증토큰이 이상한가  ");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(AuthStatus.PHONE_AUTH_FAIL));
            }
            Long groupId = managerJoinRequest.getGroupInfo().getGroupId();
            // 🔒 그룹 ID가 있으면 → 해당 그룹에 이미 담당자가 있는지 검사
            if (groupId != null) {
                boolean alreadyExists = managerRepository.existsByGroupId(groupId);
                if (alreadyExists) {
                    log.warn("❌ 그룹 ID {} 에는 이미 담당자가 등록되어 있습니다.", groupId);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse(AuthStatus.MANAGER_ALREADY_EXISTS));
                }
            }
            commonInfo.setPhoneNumber(phoneNumber);
            commonInfo.setRole(Role.ROLE_MEMBER.name());
            log.info("[POST] 🎈 expertJoinRequest 저장된전화번호 '{}' :  ", commonInfo.getPhoneNumber());
            log.info("[POST] 🎈 expertJoinRequest 저장된 롤 '{}' :  ", commonInfo.getRole());

            CommonInfo savedUserDTO = firebaseService.createMember(commonInfo); // FirebaseAuthException

            AuthStatus authStatus = authService.createManagerJoinRequest(managerJoinRequest,savedUserDTO);    // UserAlreadyExistsException

            firebaseService.setFirebaseMemberRoleToMember(savedUserDTO);    // UserNotFoundException

            if (authStatus == AuthStatus.MANAGER_JOIN_REQUEST_SUCCESS) {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(authStatus));

            } else {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(authStatus));

            }

        } catch (FirebaseAuthException firebaseAuthException) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(AuthStatus.SERVER_ERROR));


        } catch (UserAlreadyExistsException userAlreadyExistsException){

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(AuthStatus.USER_DUPLICATE));

        }
    }



    @GetMapping("/oauth2/login")
    public String loginSocial(){
        log.info("[GET] 🟢 AuthController.loginSocial");
        return "auth";
    }
    @PostMapping("/oauth2/login")
    public ResponseEntity<ApiResponse> getToken(@RequestHeader("Authorization") String authHeader){
        log.info("[POST] 🎈 AuthController.getToken");
        try{

            String idToken = jwtUtil.removeBearerPrefix(authHeader);

            FirebaseToken decoded = firebaseService.verifyIdToken(idToken);

            String jwt = jwtUtil.createToken(decoded);

            ResponseCookie jwtCookie = jwtUtil.createCookie(jwt);

            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE,jwtCookie.toString())
                    .body(new ApiResponse(UserStatus.SOCIAL_LOGIN_SUCCESS));

        } catch (JwtTokenNotFoundException jwtTokenNotFoundException){

            jwtTokenNotFoundException.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(AuthStatus.TOKEN_NOT_FOUND));

        } catch (JwtTokenFormatInvalidException jwtTokenFormatInvalidException){

            jwtTokenFormatInvalidException.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(AuthStatus.TOKEN_INVALID_FORMAT));

        } catch (FirebaseAuthException firebaseAuthException){

            firebaseAuthException.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(UserStatus.SOCIAL_LOGIN_FAIL));

        }
    }

    @GetMapping("/loginForm")
    public String showLoginForm() {
        log.info("[GET] 🟢 로그인 폼 요청");
        return "loginForm";
    }
    @GetMapping("/joinForm")
    public String showJoinForm() {
        log.info("[GET] 🟢 회원가입 폼 요청");
        return "joinForm";
    }
    @GetMapping("/expertJoinForm")
    public String showExpertJoinForm() {
        log.info("[GET] 🟢 전문가 회원가입 폼 요청");
        return "expertJoin";
    }
    @GetMapping("/managerJoinForm")
    public String showManagerJoinForm() {
        log.info("[GET] 🟢 담당자 회원가입 폼 요청");
        return "managerJoin";
    }

    @GetMapping("/updateRole/admin")
    public String updateRoleAdmin(){

        log.info("🟠 AuthController.updateRoleAdmin 요청");

        try{

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uuid = (String) authentication.getPrincipal();
            UserDTO userDTO = userService.getMemberByUUID(uuid);

            UserDTO roleToAdmin = userService.changeRoleToAdmin(userDTO);
            UserDTO dto = firebaseService.setFirebaseMemberRoleToMember(roleToAdmin);

            firebaseService.refreshToken(dto.getUuid());

        } catch (FirebaseAuthException e){
            log.info("⚠️ AuthController.changeRoleAdmin FirebaseAuthException");
        }
        return "redirect:/logout";

    }
    @GetMapping("/updateRole/manager")
    public String updateRoleManager(){

        log.info("🟠 AuthController.updateRoleManager 요청");

        try{

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uuid = (String) authentication.getPrincipal();
            UserDTO userDTO = userService.getMemberByUUID(uuid);

            UserDTO roleToAdmin = userService.changeRoleToManager(userDTO);
            UserDTO dto = firebaseService.setFirebaseMemberRoleToMember(roleToAdmin);

            firebaseService.refreshToken(dto.getUuid());

        } catch (FirebaseAuthException e){
            log.info("⚠️ AuthController.changeRoleAdmin FirebaseAuthException");
        }

        return "redirect:/logout";
    }
    @GetMapping("/updateRole/expert")
    public String updateRoleExpert(){

        log.info("🟠 AuthController.updateRoleExpert 요청");

        try{

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uuid = (String) authentication.getPrincipal();
            UserDTO userDTO = userService.getMemberByUUID(uuid);

            UserDTO roleToAdmin = userService.changeRoleToExpert(userDTO);
            UserDTO dto = firebaseService.setFirebaseMemberRoleToMember(roleToAdmin);

            firebaseService.refreshToken(dto.getUuid());

        } catch (FirebaseAuthException e){
            log.info("⚠️ AuthController.changeRoleAdmin FirebaseAuthException");
        }
        return "redirect:/logout";
    }
    @GetMapping("/updateRole/member")
    public String updateRoleMember(){

        log.info("🟠 AuthController.updateRoleMember 요청");

        try{

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uuid = (String) authentication.getPrincipal();

            UserDTO userDTO = userService.getMemberByUUID(uuid);
            UserDTO roleToAdmin = userService.changeRoleToMember(userDTO);
            UserDTO dto = firebaseService.setFirebaseMemberRoleToMember(roleToAdmin);

            firebaseService.refreshToken(dto.getUuid());

        } catch (FirebaseAuthException e){
            log.info("⚠️ AuthController.changeRoleAdmin FirebaseAuthException");
        }
        return "redirect:/logout";
    }

    @PostMapping("/joinManager/list")
    public ResponseEntity<ApiResponse> joinManagerList(@RequestBody List<ManagerJoinRequest> managerJoinRequestList){
        try{
            managerJoinRequestList.forEach(manager -> log.info(manager.getCommonInfo().getName()) );
//            managerJoinRequestList.forEach(manager -> authService.createManagerJoinRequest(manager) );

        } catch (UserAlreadyExistsException userAlreadyExistsException){

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(AuthStatus.USER_DUPLICATE));

        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(GlobalStatus.CREATED, "총 " + managerJoinRequestList.size() + "건 처리 완료"));
    }
    @PostMapping("/joinExpert/list")
    public ResponseEntity<ApiResponse> joinExpertList(@RequestBody List<ExpertJoinRequest> expertJoinRequestList){
        try{
            expertJoinRequestList.forEach(expert -> log.info(expert.getCommonInfo().getName()));
//            expertJoinRequestList.forEach(expert -> authService.createExpertJoinRequest(expert));

        } catch (UserAlreadyExistsException userAlreadyExistsException){

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(AuthStatus.USER_DUPLICATE));

        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(GlobalStatus.CREATED, "총 " + expertJoinRequestList.size() + "건 처리 완료"));
    }
}
