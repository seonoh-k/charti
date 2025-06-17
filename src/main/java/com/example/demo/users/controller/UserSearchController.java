package com.example.demo.users.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자용 사용자 검색 전용 컨트롤러
 * - 이름(name) 또는 닉네임(nickname) 기준으로 검색
 * - 포인트 지급 등 관리자 기능에서 활용
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")  // 경로: /api/user/search?type=name&value=홍길동
@Slf4j
public class UserSearchController {

    private final UserService userService;

    /**
     * 🔍 사용자 검색 API
     * @param type 검색 필터 (name | nickname)
     * @param value 검색할 키워드
     * @return 검색된 사용자 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam String type,
            @RequestParam String value
    ) {
        log.info("🔍 [관리자] 사용자 검색 요청 - type: {}, value: {}", type, value);
        return ResponseEntity.ok(userService.searchUsers(value, type));
    }
}
