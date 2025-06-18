package com.example.demo.survey.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/survey/record")
@RequiredArgsConstructor
public class RecordAnswerController {

    private final ChildService childService;
    private final RecordAnswerService recordAnswerService;
    private final AuthService authService;
    private final UserService userService;

    // 이력 페이지 접근 (HTML)
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

    // 특정 자녀의 답변 이력
    @GetMapping("/history/{childId}")
    @ResponseBody
    public List<RecordAnswerResponse> getAnswersByChild(@PathVariable Long childId) {
        try {
            UserDTO userDTO = authService.getLoginUser();
            Member loginUser = userService.getMemberEntityById(userDTO.getId());
            Child child = childService.findById(childId);

            List<RecordAnswer> answers = recordAnswerService.getAnswersByWriterAndChild(loginUser, child);

            return answers.stream()
                    .map(RecordAnswerResponse::fromEntity)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/answer/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAnswer(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newAnswer = body.get("answer");
        recordAnswerService.updateAnswerText(id, newAnswer);
        return ResponseEntity.ok().build();
    }


    // 답변 soft delete
    @DeleteMapping("/answer/{id}")
    @ResponseBody
    public String deleteAnswer(@PathVariable Long id) {
        try {
            authService.getLoginUser(); // 로그인 확인용 (사용은 안하지만 필수)
            recordAnswerService.softDelete(id);
            return "success";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
