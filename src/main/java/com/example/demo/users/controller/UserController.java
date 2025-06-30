package com.example.demo.users.controller;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.AdminDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.request.PasswordChangeRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserInfoResponse;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.AdminNotFoundException;
import com.example.demo.users.repository.AdminRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.AdminService;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.GlobalStatus;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AdminService adminService;
    private final AdminRepository adminRepository;

    @GetMapping("/users/me")
    public ResponseEntity<UserDTO> getMyInfo(Authentication authentication) {
        String uid = authentication.getPrincipal().toString();
        Users user = userService.findByUuidEntity(uid);
        return ResponseEntity.ok(new UserDTO(user));
    }
    // admin 레스트 컨트롤러 만들면 옮겨서 분리 필요
    @GetMapping("/admin/me")
    public ResponseEntity<?> getAdminInfo(Authentication authentication) {
        try {
            String uuid = authentication.getPrincipal().toString();

            Optional<AdminDTO> optional = adminRepository.getAdminDTOByUUIDToAuth(uuid);

            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("관리자 정보 없음");
            }

            return ResponseEntity.ok(optional.get());
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

//    @PutMapping("/update")
//    public ResponseEntity<?> updateUser(@RequestBody UserUpdateRequest request,
//                                        Authentication authentication) {
//        String uid = authentication.getPrincipal().toString();
//        try {
//
//            userService.updateUser(request, uid);
//            return ResponseEntity.ok(new ApiResponse(UserStatus.UPDATE_SUCCESS));
//
//        } catch (FirebaseAuthException e) {
//            return ResponseEntity.internalServerError()
//                    .body(new ApiResponse(GlobalStatus.FIREBASE_ERROR, "Firebase 업데이트 실패"));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(GlobalStatus.AUTHENTICATION_FAIL, e.getMessage()));
//        }
//    }

    @PutMapping("/users/update/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request,
                                            Authentication authentication) {
        String uid = authentication.getPrincipal().toString();
        try {
            userService.changePassword(uid,
                    request.getCurrentPassword(),
                    request.getNewPassword(),
                    request.getConfirmPassword());
            return ResponseEntity.ok(new ApiResponse(AuthStatus.PASSWORD_CHANGE_SUCCESS));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(GlobalStatus.AUTHENTICATION_FAIL,e.getMessage()));
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }


    @DeleteMapping("/users/delete")
    public ResponseEntity<?> deleteUser(Authentication authentication) {
        String uid = authentication.getPrincipal().toString();
        log.info("🔴 사용자 탈퇴 요청 - UID: {}", uid);

        userService.softDeleteUser(uid);

        // 쿠키 제거
        ResponseCookie deleteCookie = ResponseCookie.from("token", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(new ApiResponse(AuthStatus.USER_DELETED_SUCCESS));
    }


}
