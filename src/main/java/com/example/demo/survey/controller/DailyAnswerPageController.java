package com.example.demo.survey.controller;

import com.example.demo.survey.dto.DailyAnswerDto;
import com.example.demo.survey.dto.DailyAnswerRequest;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.service.DailyAnswerService;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dailyAnswer")
public class DailyAnswerPageController {

    private final UserService userService;
    private final ChildService childService;
    private final DailyAnswerService answerService;

    /** 뷰: 이력 페이지 */
    @GetMapping("/history")
    public String showHistoryPage(Authentication auth, Model model) {
        Users me;
        try {
            me = userService.findByUsernameEntity(auth.getName());
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(auth.getName());
        }
        List<Child> children = childService.findByUsersId(me.getId());
        model.addAttribute("children", children);
        return "survey/dailyAnswerHistory";
    }

    /** API: 특정 자녀의 답변 이력 조회 */
    @GetMapping("/api/history/{childId}")
    @ResponseBody
    public List<DailyAnswerDto> getHistoryByChild(@PathVariable Long childId) {
        return answerService.getAnswersByChild(childId).stream()
                .map(da -> {
                    // 나이 계산
                    LocalDate bday = da.getChild().getBirthday().toLocalDate();
                    int age = Period.between(bday, LocalDate.now()).getYears();
                    String childDisplay = da.getChild().getName()
                            + " (" + da.getChild().getNickname() + ") - " + age + "세";
                    DailySurvey s = da.getSurvey();
                    List<String> opts = List.of(
                            s.getAnswer1(), s.getAnswer2(), s.getAnswer3(),
                            s.getAnswer4(), s.getAnswer5()
                    );
                    // da.getAnswer() 에 저장된 텍스트이므로, 몇 번째인지 찾아서 selectedValue 로 전달
                    int sel = opts.indexOf(da.getAnswer()) + 1;
                    return new DailyAnswerDto(
                            da.getId(),
                            childDisplay,
                            da.getCategory().getDisplayName(),
                            s.getAgeGroup().getDisplayName(),
                            da.getQuestion(),
                            da.getAnswer(),
                            da.getWeight(),
                            da.getCreatedAt(),
                            opts,
                            sel
                    );
                })
                .collect(Collectors.toList());
    }

    /** API: 답변 수정 */
    @PutMapping("/api/answer/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAnswer(
            @PathVariable Long id,
            @RequestBody DailyAnswerRequest req   // DTO 로 받는다!
    ) {
        answerService.updateAnswerValue(id, req.getAnswerValue());
        return ResponseEntity.ok().build();
    }

    /** API: 답변 삭제(soft delete) */
    @DeleteMapping("/api/answer/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.ok().build();
    }
}
