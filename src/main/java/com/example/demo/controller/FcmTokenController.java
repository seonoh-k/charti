//package com.example.demo.controller;
//
//import com.example.demo.users.entity.Users;
//import com.example.demo.users.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/user")
//@RequiredArgsConstructor
//public class FcmTokenController {
//
//    private final UserRepository userRepository;
//
//    // 로그인 이후, 클라이언트에서 FCM 토큰 전달
//    @PostMapping("/fcm-token")
//    public ResponseEntity<?> saveFcmToken(@AuthenticationPrincipal UserDetails userDetails,
//                                          @RequestBody Map<String, String> request) {
//
//        String newToken = request.get("fcmToken");
//
//        // 현재 로그인된 사용자 기준으로 DB 업데이트
//        Optional<Users> optionalUser = userRepository.findByUsername(userDetails.getUsername());
//
//        if (optionalUser.isPresent()) {
//            Users user = optionalUser.get();
//
//            // 기존 토큰과 다를 때만 업데이트
//            if (!newToken.equals(user.getFcmToken())) {
//                user.setFcmToken(newToken);
//                userRepository.save(user);
//            }
//
//            return ResponseEntity.ok().body("토큰 저장 완료");
//        }
//
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 정보를 찾을 수 없음");
//    }
//}
