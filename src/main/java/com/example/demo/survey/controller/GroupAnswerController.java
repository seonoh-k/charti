package com.example.demo.survey.controller;

import com.example.demo.survey.dto.AnswerUpdateRequest;
import com.example.demo.survey.dto.GroupAnswerDto;
import com.example.demo.survey.dto.GroupAnswerRequest;
import com.example.demo.survey.service.GroupAnswerService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/groupAnswer")
@RequiredArgsConstructor
public class GroupAnswerController {

    private final UserService userService;
    private final ChildService childService;
    private final GroupAnswerService answerService;

    /** 뷰: 이력 페이지 */
    @GetMapping("/history")
    public String historyPage(Authentication auth, Model model) {
        Users me;
        try {
            me = userService.findByUsernameEntity(auth.getName());
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(auth.getName());
        }
        List<Child> children = childService.findByUsersId(me.getId());
        model.addAttribute("children", children);
        return "survey/groupAnswerHistory";
    }

    /** API: 특정 자녀의 답변 이력 조회 */
    @GetMapping("/api/history/{childId}")
    @ResponseBody
    public List<GroupAnswerDto> apiHistory(@PathVariable Long childId) {
        return answerService.findByChild(childId).stream()
                .map(GroupAnswerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** API: 답변 수정 */
    @PutMapping("/api/answer/{id}")
    @ResponseBody
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody AnswerUpdateRequest req) {
        answerService.updateAnswerValue(id, req.answerValue());
        return ResponseEntity.ok().build();
    }

    /** API: 답변 삭제(soft delete) */
    @DeleteMapping("/api/answer/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.ok().build();
    }
}
