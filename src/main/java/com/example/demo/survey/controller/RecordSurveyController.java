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
     */
    @PostMapping("/submit")
    public String submitSurvey(@RequestParam Long childId, HttpServletRequest request) { // HttpServletRequest를 파라미터로 받음
        log.info("--- submitSurvey 메서드 시작 ---");
        log.info("요청 파라미터 - childId: {}", childId);

        List<RecordSurveyAnswerDto> answers = new ArrayList<>();
        // HttpServletRequest를 사용하여 answers[i].questionId 와 answers[i].text 파라미터를 수동으로 파싱
        int i = 0;
        while (request.getParameter("answers[" + i + "].questionId") != null) {
            Long questionId = Long.valueOf(request.getParameter("answers[" + i + "].questionId"));
            String text = request.getParameter("answers[" + i + "].text");

            RecordSurveyAnswerDto dto = new RecordSurveyAnswerDto();
            dto.setQuestionId(questionId);
            dto.setText(text);
            answers.add(dto);
            i++;
        }

        log.info("요청 파라미터 - answers 리스트 크기: {}", answers.size()); // 이제 여기서는 오류 안 날 것
        if (answers != null) {
            for (int j = 0; j < answers.size(); j++) { // for (int i = 0; ... )와 충돌 방지
                RecordSurveyAnswerDto dto = answers.get(j);
                log.info("  answers[{}] - questionId: {}, text: {}", j, dto.getQuestionId(), dto.getText());
            }
        }

        try {
            // ... (기존 submitSurvey 로직 계속)
            UserDTO userDTO = authService.getLoginUser();
            Member loginUser = userService.getMemberEntityById(userDTO.getId());
            Child child = childService.findById(childId);

            for (RecordSurveyAnswerDto dto : answers) { // 이제 answers는 안전한 List<RecordSurveyAnswerDto>
                recordAnswerService.saveAnswer(loginUser, child, dto);
            }

            return "redirect:/survey/record/history";
        } catch (Exception e) {
            log.error("--- submitSurvey 메서드 중 예상치 못한 오류 발생 ---", e);
            log.error("오류 발생 시 childId: {}", childId);
            return "redirect:/loginForm";
        }
    }
}
