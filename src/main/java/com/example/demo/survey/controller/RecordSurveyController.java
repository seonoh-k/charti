package com.example.demo.survey.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.dto.RecordSurveyAnswerDto;
import com.example.demo.survey.dto.RecordSurveyDataResponse;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.survey.service.RecordSurveyService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
     * 기록문진 페이지 진입 시 회원 정보 및 자녀 리스트 전달
     * 선택된 자녀가 있다면 해당 자녀 정보 및 질문 목록도 함께 제공
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

            if (childId != null) {
                Child selectedChild = childService.findById(childId);
                model.addAttribute("selectedChild", selectedChild);

                AgeGroup ageGroup = selectedChild.getAgeGroup();
                List<RecordSurvey> surveys = recordSurveyService.getSurveysByAgeGroup(ageGroup);
                model.addAttribute("surveys", surveys);
            }

            return "survey/recordAnswerForm";
        } catch (Exception e) {
            return "redirect:/loginForm";
        }
    }

    /**
     * 자녀 ID를 통한 문진 질문 Ajax 요청 처리
     */
    @GetMapping("/child/{childId}")
    @ResponseBody
    public RecordSurveyDataResponse getSurveyByChild(@PathVariable Long childId) {
        Child child = childService.findById(childId);
        AgeGroup ageGroup = child.getAgeGroup();
        List<RecordSurvey> surveys = recordSurveyService.getSurveysByAgeGroup(ageGroup);
        return new RecordSurveyDataResponse(child, surveys);
    }

    /**
     * 문진 응답 제출 처리
     * - HTML form에서 전달된 질문/답변 리스트를 파싱하여
     * - 답변 저장 + 포인트 지급 처리까지 일괄 수행
     */
    @PostMapping("/submit")
    public String submitSurvey(@RequestParam Long childId, HttpServletRequest request) {
        log.info("--- submitSurvey 메서드 시작 ---");
        log.info("요청 파라미터 - childId: {}", childId);

        List<RecordSurveyAnswerDto> answers = new ArrayList<>();
        int i = 0;

        // 폼에서 전송된 answers[i].questionId 와 answers[i].text 파라미터 수동 파싱
        while (request.getParameter("answers[" + i + "].questionId") != null) {
            Long questionId = Long.valueOf(request.getParameter("answers[" + i + "].questionId"));
            String text = request.getParameter("answers[" + i + "].text");

            RecordSurveyAnswerDto dto = new RecordSurveyAnswerDto();
            dto.setQuestionId(questionId);
            dto.setText(text);
            dto.setChildId(childId); // ✅ 추가 필요: submitRecordAnswers()에서 childId 사용
            answers.add(dto);
            i++;
        }

        log.info("총 {}개의 문진 답변 파싱 완료", answers.size());

        try {
            UserDTO userDTO = authService.getLoginUser();
            Member loginUser = userService.getMemberEntityById(userDTO.getId());

            // ✅ 저장 및 포인트 지급 통합 처리
            recordAnswerService.submitRecordAnswers(loginUser, answers);

            return "redirect:/survey/record/history";
        } catch (Exception e) {
            log.error("--- submitSurvey 메서드 중 예외 발생 ---", e);
            return "redirect:/loginForm";
        }
    }

}
