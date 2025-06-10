package com.example.demo.controller;

import com.example.demo.dto.PointChangeRequest;
import com.example.demo.dto.PointHistoryView;
import com.example.demo.entity.PointHistory;
import com.example.demo.service.PointService;
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

    // 포인트 증가 / 차감
    @PostMapping("/change")
    public ResponseEntity<?> changePoint(@Valid @RequestBody PointChangeRequest req,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 에러 메시지 반환 (첫 번째 오류 메시지만 반환)
            String errorMsg = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMsg);
        }

        pointService.changePoint(req.getMemberId(), req.getAmount(), req.getDescription());
        return ResponseEntity.ok().build();
    }



    // 조회[마이페이지 등등]
    @GetMapping("/current")
    public ResponseEntity<Integer> getCurrentPoint(@RequestParam Long memberId) {
        return ResponseEntity.ok(pointService.getCurrentPoint(memberId));
    }

    // 변동 내역 조회
    @GetMapping("/history")
    public ResponseEntity<List<PointHistory>> getPointHistory(@RequestParam Long memberId) {
        return ResponseEntity.ok(pointService.getPointHistory(memberId));
    }

    @GetMapping("/history/view")
    public ResponseEntity<List<PointHistoryView>> getViewHistory(@RequestParam Long memberId) {
        return ResponseEntity.ok(pointService.getFormattedHistory(memberId));
    }

}
