package com.example.demo.users.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.request.ExpertJoinRequest;
import com.example.demo.dto.request.ManagerJoinRequest;
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
import com.example.demo.util.GlobalStatus;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * @param userDTO : 폼 양식으로 받은 DTO 객체
     * @return
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse> joinMember(@RequestBody UserDTO userDTO) {
        // 1. 유효성 검사 실패하면 파이어베이스 계정 삭제
        try {

            FirebaseToken firebaseToken = firebaseService.verifyIdToken(userDTO.getSmsIdToken());

            String uid = firebaseToken.getUid();
            firebaseService.deleteFirebaseMember(uid);
            String phoneNumber = (String) firebaseToken.getClaims().get("phone_number");

            // 인증 실패: 잘못된 토큰이거나 SMS 인증이 아님
            if (phoneNumber == null) {
                // code : "PAF", message : "sms 인증 실패"
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(AuthStatus.PHONE_AUTH_FAIL));
            }

            userDTO.setPhoneNumber(phoneNumber);
            userDTO.setRole(Role.ROLE_MEMBER.name());

            UserDTO savedUserDTO = firebaseService.createMember(userDTO);

            UserStatus userStatus = userService.createMember(savedUserDTO);

            firebaseService.setFirebaseMemberRoleToMember(savedUserDTO);

            if (userStatus == UserStatus.JOIN_SUCCESS) {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(userStatus));

            } else {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(userStatus));

            }

        } catch (FirebaseAuthException firebaseAuthException) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(AuthStatus.SERVER_ERROR));

        } catch (IllegalArgumentException illegalArgumentException) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(AuthStatus.AUTHENTICATION_FAIL));

        }
    }

    @PostMapping("/joinExpert")
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
    @PostMapping("/joinManager")
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

    @PostMapping("/joinManager/list")
    public ResponseEntity<ApiResponse> joinManagerList(@RequestBody List<ManagerJoinRequest> managerJoinRequestList){
        try{
            managerJoinRequestList.forEach(manager -> log.info(manager.getCommonInfo().getName()) );
            managerJoinRequestList.forEach(manager -> authService.createManagerJoinRequest(manager) );

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
            expertJoinRequestList.forEach(expert -> authService.createExpertJoinRequest(expert));

        } catch (UserAlreadyExistsException userAlreadyExistsException){

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(AuthStatus.USER_DUPLICATE));

        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(GlobalStatus.CREATED, "총 " + expertJoinRequestList.size() + "건 처리 완료"));
    }
}
