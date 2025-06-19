package com.example.demo.controller;

import com.example.demo.dto.PointChangeRequest;
import com.example.demo.dto.PointHistoryView;
import com.example.demo.entity.PointHistory;
import com.example.demo.service.PointService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {

    private final PointService pointService;
    private final UserService userService;

    /**
     * [관리자 포인트 변경 요청]
     * - 회원 ID, 금액, 설명, 유형(PointType)을 받아 포인트를 지급하거나 차감
     * - 요청 DTO에 대한 유효성 검증 포함
     * - 지급/차감 내역은 PointHistory에 저장
     */
    @PostMapping("/change")
    public ResponseEntity<?> changePoint(@Valid @RequestBody PointChangeRequest req,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 유효성 검사 실패 시 첫 번째 오류 메시지 반환
            String errorMsg = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMsg);
        }

        // 회원 정보 조회
        Member member = userService.getMemberEntityById(req.getMemberId());

        // 통합 포인트 지급 메서드 호출
        pointService.givePoint(
                member,
                req.getAmount(),
                req.getDescription(),
                req.getPointType(),
                null // 관리자 지급 시 자녀 정보 없음
        );

        return ResponseEntity.ok().build();
    }

    /**
     * [회원 현재 포인트 조회]
     * - 단건 조회용
     */
    @GetMapping("/current")
    public ResponseEntity<Integer> getCurrentPoint(@RequestParam Long memberId) {
        return ResponseEntity.ok(pointService.getCurrentPoint(memberId));
    }

    /**
     * [회원 포인트 변동 이력 원본 조회]
     * - PointHistory 리스트 그대로 반환 (관리자 또는 개발용)
     */
    @GetMapping("/history")
    public ResponseEntity<List<PointHistory>> getPointHistory(@RequestParam Long memberId) {
        return ResponseEntity.ok(pointService.getPointHistory(memberId));
    }

    /**
     * [회원 포인트 변동 이력 뷰용 조회]
     * - beforePoint, afterPoint, description 등 가공된 View DTO 리스트 반환
     * - 화면에 최근 포인트 변동을 표시할 때 사용
     */
    @GetMapping("/history/view")
    public ResponseEntity<List<PointHistoryView>> getViewHistory(@RequestParam Long memberId) {
        return ResponseEntity.ok(pointService.getFormattedHistory(memberId));
    }

}
