package com.example.demo.survey.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResponse;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 기록 문진 답변 관련 사용자 기능을 처리하는 컨트롤러입니다.
 * - 자녀 선택 후 기록 문진 이력 조회
 * - 답변 수정 및 삭제 처리
 */
@Controller
@RequestMapping("/survey/record")
@RequiredArgsConstructor
public class RecordAnswerController {

    private final ChildService childService;
    private final RecordAnswerService recordAnswerService;
    private final AuthService authService;
    private final UserService userService;

    /**
     * 기록 문진 이력 페이지를 표시합니다.
     * 로그인한 사용자의 자녀 목록을 모델에 추가하여 Thymeleaf 템플릿에 전달합니다.
     *
     * @param model Thymeleaf 템플릿으로 전달할 데이터 모델
     * @return 기록 문진 이력 페이지 뷰 이름
     */
    @GetMapping("/history")
    public String showAnswerHistory(Model model) {
        try {
            UserDTO userDTO = authService.getLoginUser();
            Member loginUser = userService.getMemberEntityById(userDTO.getId());
            List<Child> children = childService.getChildrenByMember(loginUser);
            model.addAttribute("children", children);
            return "survey/recordAnswerHistory";
        } catch (Exception e) {
            return "redirect:/loginForm";
        }
    }

    /**
     * 특정 자녀의 기록 문진 답변 이력을 페이지 단위로 조회합니다.
     *
     * @param childId        자녀 ID
     * @param pagingRequest  페이징 요청 정보
     * @return 페이징된 답변 이력 응답
     */
    @GetMapping("/history/{childId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<PagingResponse<RecordAnswerResponse>>> getRecordAnswersHistory(
            @PathVariable Long childId,
            @ModelAttribute PagingRequest pagingRequest) {
        PagingResponse<RecordAnswerResponse> pagingResponse = recordAnswerService.getRecordAnswersPageByChild(childId, pagingRequest);
        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, pagingResponse));
    }

    /**
     * 특정 답변의 텍스트 내용을 수정합니다.
     *
     * @param id   수정할 답변 ID
     * @param body 요청 본문: {"answer": "새 답변"}
     * @return HTTP 200 OK 응답
     */
    @PutMapping("/answer/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAnswer(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newAnswer = body.get("answer");
        if (newAnswer == null || newAnswer.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변 내용은 비워둘 수 없습니다.");
        }
        recordAnswerService.updateAnswerText(id, newAnswer);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 답변을 소프트 삭제합니다.
     *
     * @param id 삭제할 답변 ID
     * @return 삭제 성공 시 "success" 문자열 반환
     */
    @DeleteMapping("/answer/{id}")
    @ResponseBody
    public String deleteAnswer(@PathVariable Long id) {
        try {
            authService.getLoginUser();
            recordAnswerService.softDelete(id);
            return "success";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "답변 삭제 권한이 없거나 오류가 발생했습니다.");
        }
    }
}
