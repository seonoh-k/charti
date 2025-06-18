package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.DailyAnswerDto;
import com.example.demo.survey.entity.DailyAnswer;
import com.example.demo.survey.repository.DailyAnswerRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class DailyAnswerPageController {

    private final UserService           userService;
    private final ChildService          childService;
    private final DailyAnswerRepository answerRepo;

    /** 뷰: 필터용 드롭다운 데이터만 전달 */
    @GetMapping("/dailyAnswer")
    public String showAnswerPage(Authentication auth, Model model) {
        String principal = auth.getName();
        Users me;
        try {
            me = userService.findByUsernameEntity(principal);
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(principal);
        }

        model.addAttribute("children",
                childService.findByUsersId(me.getId()));
        model.addAttribute("categories",
                Arrays.stream(SurveyCategory.values())
                        .filter(c -> c != SurveyCategory.ALL && c != SurveyCategory.VARIOUS)
                        .toList());
        model.addAttribute("ageGroups",
                Arrays.stream(AgeGroup.values())
                        .filter(a -> a != AgeGroup.ALL && a != AgeGroup.VARIOUS)
                        .toList());

        return "dailyAnswer";
    }

    /** API: 페이징·필터링된 응답 JSON 반환 */
    @GetMapping("/api/answers")
    @ResponseBody
    public Map<String,Object> getAnswers(
            @RequestParam(required = false) Long childId,
            @RequestParam(required = false) SurveyCategory category,
            @RequestParam(required = false) AgeGroup ageGroup,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 4);
        Page<DailyAnswer> paged = answerRepo.findByFilters(childId, category, ageGroup, pageable);

        // content → DTO
        List<DailyAnswerDto> dtos = paged.map(da -> {
            // 자녀 display
            LocalDate bday = da.getChild().getBirthday().toLocalDate();
            int age = Period.between(bday, LocalDate.now()).getYears();
            String childDisplay = da.getChild().getName()
                    + " (" + da.getChild().getNickname() + ") - " + age + "세";
            return new DailyAnswerDto(
                    da.getId(),
                    childDisplay,
                    da.getCategory().getDisplayName(),
                    da.getSurvey().getAgeGroup().getDisplayName(),
                    da.getQuestion(),
                    da.getAnswer(),
                    da.getWeight(),
                    da.getCreatedAt()
            );
        }).getContent();

        // 메타데이터 포함
        Map<String,Object> result = new HashMap<>();
        result.put("content",       dtos);
        result.put("currentPage",   paged.getNumber());
        result.put("totalPages",    paged.getTotalPages());
        result.put("totalElements", paged.getTotalElements());
        return result;
    }
}
