package com.example.demo.survey.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.dto.RecordSurveyAnswerDto;
import com.example.demo.survey.dto.RecordSurveyDataResponse;
import com.example.demo.survey.dto.RecordSurveyResponse;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.survey.service.RecordSurveyService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 기록문진 사용자 컨트롤러
 * - 문진 페이지 진입, 자녀별 질문 조회, 문진 제출 처리 기능 제공
 */
@Slf4j
@Controller
@RequestMapping("/survey/record")
@RequiredArgsConstructor
public class RecordSurveyController {

    private final ChildService childService;
    private final RecordSurveyService recordSurveyService;
    private final RecordAnswerService recordAnswerService;
    private final AuthService authService;
    private final UserService userService;

    /**
     * 기록문진 페이지 진입 시 회원 및 자녀 정보, 문진 질문 전달
     * @param childId 선택된 자녀 ID (선택사항)
     * @param model Thymeleaf 모델
     * @return 기록문진 입력 폼 페이지
     */
    @GetMapping
    public String showSurveyPage(@RequestParam(value = "childId", required = false) Long childId,
                                 Model model) {
        try {
            UserDTO userDTO = authService.getLoginUser();
            Member loginUser = userService.getMemberEntityById(userDTO.getId());

            List<Child> children = childService.getChildrenByMember(loginUser);
            model.addAttribute("member", loginUser);
            model.addAttribute("children", children);

            Child selectedChild = null;
            boolean isAlreadyWritten = false;

            if (childId != null) {
                selectedChild = childService.findById(childId);
                if (selectedChild != null && selectedChild.getParent().getId().equals(loginUser.getId())) {
                    model.addAttribute("selectedChild", selectedChild);
                    isAlreadyWritten = recordAnswerService.hasAnsweredToday(selectedChild);
                    model.addAttribute("isAlreadyWritten", isAlreadyWritten);

                    if (!isAlreadyWritten) {
                        AgeGroup ageGroup = selectedChild.getAgeGroup();
                        List<RecordSurvey> surveys = recordSurveyService.getSurveysByAgeGroup(ageGroup);
                        model.addAttribute("surveys", surveys);
                    } else {
                        model.addAttribute("surveys", null);
                    }
                } else {
                    log.warn("Invalid or unauthorized childId provided: {}", childId);
                    model.addAttribute("selectedChild", null);
                    model.addAttribute("surveys", null);
                }
            } else if (!children.isEmpty()) {
                model.addAttribute("selectedChild", null);
                model.addAttribute("surveys", null);
                model.addAttribute("isAlreadyWritten", false);
            }

            return "survey/recordAnswerForm";
        } catch (Exception e) {
            log.error("기록 문진 페이지 로드 중 오류 발생", e);
            model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
            return "errorPage";
        }
    }

    /**
     * 자녀 ID를 통한 문진 질문 Ajax 요청 처리
     * @param childId 자녀 ID
     * @return 자녀 정보 + 문진 질문 리스트 응답
     */
//    @GetMapping("/child/{childId}")
//    @ResponseBody
//    public RecordSurveyDataResponse getSurveyByChild(@PathVariable Long childId) {
//        Child child = childService.findById(childId);
//        if (child == null) {
//            log.warn("Child not found for ID: {}", childId);
//            return new RecordSurveyDataResponse(null, new ArrayList<>());
//        }
//
//        if (recordAnswerService.hasAnsweredToday(child)) {
//            log.info("Child ID '{}' has already submitted a record survey today.", childId);
//            return new RecordSurveyDataResponse(child, new ArrayList<>());
//        }
//
//        AgeGroup ageGroup = child.getAgeGroup();
//        List<RecordSurvey> surveys = recordSurveyService.getSurveysByAgeGroup(ageGroup);
//        return new RecordSurveyDataResponse(child, surveys);
//    }

    @GetMapping("/child/{childId}")
    @ResponseBody
    public RecordSurveyDataResponse getSurveyByChild(@PathVariable Long childId) {
        Child child = childService.findById(childId);
        if (child == null) {
            log.warn("Child not found for ID: {}", childId);
            return new RecordSurveyDataResponse(null, new ArrayList<>());
        }

        if (recordAnswerService.hasAnsweredToday(child)) {
            log.info("Child ID '{}' has already submitted a record survey today.", childId);
            return new RecordSurveyDataResponse(child, new ArrayList<>());
        }

        AgeGroup ageGroup = child.getAgeGroup();
        List<RecordSurvey> surveys = recordSurveyService.getSurveysByAgeGroup(ageGroup);

        // ✅ RecordSurvey → RecordSurveyResponse 변환
        List<RecordSurveyResponse> surveyResponses = surveys.stream()
                .map(RecordSurveyResponse::fromEntity)
                .collect(Collectors.toList());

        return new RecordSurveyDataResponse(child, surveyResponses);
    }
    /**
     * 문진 응답 제출 처리
     * @param requestList 문진 응답 리스트
     * @return 저장 성공 또는 오류 메시지 응답
     */
    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<?> submitSurvey(@RequestBody List<RecordSurveyAnswerDto> requestList) {
        log.info("--- submitSurvey 메서드 시작 ---");
        log.info("요청 JSON 데이터 수신. 답변 개수: {}", requestList != null ? requestList.size() : 0);

        if (requestList == null || requestList.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("문진 답변이 비어있습니다."));
        }

        try {
            UserDTO userDTO = authService.getLoginUser();
            Member loginUser = userService.getMemberEntityById(userDTO.getId());

            Long childId = requestList.get(0).getChildId();
            Child child = childService.findById(childId);

            if (child == null || !child.getParent().getId().equals(loginUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("유효하지 않거나 접근 권한이 없는 자녀 정보입니다."));
            }

            recordAnswerService.submitRecordAnswers(loginUser, requestList);
            return ResponseEntity.ok().body(new MessageResponse("문진 답변이 성공적으로 저장되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("문진 제출 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("--- submitSurvey 메서드 중 예외 발생 ---", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("서버 오류가 발생했습니다. 다시 시도해주세요."));
        }
    }

    /** 에러 응답 DTO */
    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /** 성공 메시지 응답 DTO */
    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}