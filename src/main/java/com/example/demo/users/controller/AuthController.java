package com.example.demo.users.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.info.CommonInfo;
import com.example.demo.dto.request.ExpertJoinRequest;
import com.example.demo.dto.request.ManagerJoinRequest;
import com.example.demo.dto.request.MemberJoinRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.exception.JwtTokenFormatInvalidException;
import com.example.demo.exception.JwtTokenNotFoundException;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.users.entity.Role;
import com.example.demo.users.exception.UserAlreadyExistsException;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.FirebaseService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.UserStatus;
import com.google.api.Http;
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

import java.util.Map;

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

        } catch (JwtTokenNotFoundException jwtTokenNotFoundException){

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

            MemberJoinRequest savedUserDTO = firebaseService.createMember(memberJoinRequest);

            AuthStatus authStatus = authService.createMemberJoinRequest(savedUserDTO);

            firebaseService.setFirebaseMemberRoleToMember(savedUserDTO);

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

    @PostMapping("/join/Expert")
    public ResponseEntity<ApiResponse> joinExpert(@RequestBody ExpertJoinRequest expertJoinRequest){
        try{
            AuthStatus authStatus = authService.createExpertJoinRequest(expertJoinRequest);
        } catch (UserAlreadyExistsException userAlreadyExistsException){

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(AuthStatus.USER_DUPLICATE));

        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(AuthStatus.EXPERT_JOIN_REQUEST_SUCCESS));
    }
    @PostMapping("/join/Manager")
    public ResponseEntity<ApiResponse> joinManager(@RequestBody ManagerJoinRequest managerJoinRequest){
        try{
            AuthStatus authStatus = authService.createManagerJoinRequest(managerJoinRequest);
        } catch (UserAlreadyExistsException userAlreadyExistsException){

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(AuthStatus.USER_DUPLICATE));

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(AuthStatus.MANAGER_JOIN_REQUEST_SUCCESS));
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

}
