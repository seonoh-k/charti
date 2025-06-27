package com.example.demo.users.controller;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.request.PasswordChangeRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserInfoResponse;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.GlobalStatus;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

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

    @PutMapping("/update/password")
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


    @DeleteMapping("/delete")
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
