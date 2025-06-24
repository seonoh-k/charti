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
import org.springframework.security.core.userdetails.UserDetails;
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

//    /** 뷰: 이력 페이지 */
//    @GetMapping("/history")
//    public String historyPage(Authentication auth, Model model) {
//        Users me;
//        try {
//            me = userService.findByUsernameEntity(auth.getName());
//        } catch (UserNotFoundException e) {
//            me = userService.findByUuidEntity(auth.getName());
//        }
//        model.addAttribute("children", childService.findByUsersId(me.getId()));
//        return "survey/groupAnswerHistory";
//    }
//
//    /** API: 특정 자녀의 답변 이력 조회 */
//    @GetMapping("/api/history/{childId}")
//    @ResponseBody
//    public List<GroupAnswerDto> apiHistory(@PathVariable Long childId) {
//        return answerService.findByChild(childId).stream()
//                .map(GroupAnswerDto::fromEntity)
//                .collect(Collectors.toList());
//    }

    /**
     * 현재 인증된 사용자의 Users 엔티티를 조회한다.
     *
     * @param auth 현재 인증 정보가 담긴 Authentication 객체
     * @return 인증된 사용자의 Users 엔티티
     * @throws UserNotFoundException 인증되지 않았거나, 사용자를 찾을 수 없을 경우 발생
     */
    private Users getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new UserNotFoundException("인증되지 않은 사용자입니다.");
        }

        String principalIdentifier;

        if (auth.getPrincipal() instanceof UserDetails) {
            principalIdentifier = ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            principalIdentifier = auth.getName();
        }

        try {
            return userService.findByUuidEntity(principalIdentifier);
        } catch (UserNotFoundException e) {
            try {
                return userService.findByUsernameEntity(principalIdentifier);
            } catch (UserNotFoundException ex) {
                throw new UserNotFoundException("현재 사용자를 찾을 수 없습니다: " + principalIdentifier);
            }
        }
    }


    /** 뷰: 이력 페이지 */
    @GetMapping("/history")
    public String historyPage(Authentication auth, Model model) {
        try {
            Users me = getCurrentUser(auth); // 헬퍼 메소드 사용
            model.addAttribute("children", childService.findByUsersId(me.getId()));
            return "survey/groupAnswerHistory";
        } catch (UserNotFoundException e) {
            // 사용자를 찾지 못하면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }
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
