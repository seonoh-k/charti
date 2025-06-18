package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SurveyRequestDto;
import com.example.demo.survey.entity.DailyAnswer;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.repository.DailyAnswerRepository;
import com.example.demo.survey.service.DailySurveyService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.ChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class DailySurveyController {

    private final DailySurveyService dailySurveyService;
    private final DailyAnswerRepository answerRepo;
    private final ChildService childService;
    private final MemberRepository memberRepository;

    @GetMapping("/{ageGroup}")
    @ResponseBody
    public List<DailySurvey> getSurveyListByAgeGroup(@PathVariable AgeGroup ageGroup) {
        return dailySurveyService.getSurveysByAgeGroup(ageGroup);
    }

    @PostMapping(value = "/submit",
            consumes = "application/json; charset=UTF-8",
            produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String,Object> submitSurvey(@RequestBody SurveyRequestDto dto) {
        // 1) DTO 검증
        System.out.println("====== SurveyRequestDto DTO 확인 ======");
        System.out.println("받은 ageGroup: " + dto.getAgeGroup());
        System.out.println("받은 answers: " + dto.getAnswers());
        System.out.println("===================================");

        // 2) 자녀 엔티티 조회
        Child child = childService.findById(dto.getChildId());

        // 3) 설문 목록 조회
        List<DailySurvey> surveys = dailySurveyService.getSurveysByAgeGroup(dto.getAgeGroup());
        if (surveys == null || surveys.isEmpty()) {
            throw new IllegalArgumentException("문진 데이터가 없습니다.");
        }

        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("답변이 없습니다.");
        }

        if (dto.getAnswers().size() != surveys.size()) {
            throw new IllegalArgumentException("응답 배열과 설문 데이터 크기가 일치하지 않습니다.");
        }
        // 4) 답변 저장
        for (int i = 0; i < surveys.size(); i++) {
            DailySurvey s = surveys.get(i);
            int answerIdx = dto.getAnswers().get(i);

            String answerText;
            switch (answerIdx) {
                case 1: answerText = s.getAnswer1(); break;
                case 2: answerText = s.getAnswer2(); break;
                case 3: answerText = s.getAnswer3(); break;
                case 4: answerText = s.getAnswer4(); break;
                case 5: answerText = s.getAnswer5(); break;
                default: answerText = "";
            }

            DailyAnswer da = new DailyAnswer();
            da.setChild(child);
            da.setSurvey(s);
            da.setCategory(s.getCategory());
            da.setQuestion(s.getQuestion());
            da.setAnswer(answerText);
            da.setWeight(s.getWeight());
            answerRepo.save(da);
        }

        // 5) 포인트 적립: 이 child의 부모(Member) totalPoint += 500
        Member parent = child.getParent();
        if (parent.getTotalPoint() == null) {
            parent.setTotalPoint(0);
        }
        parent.setTotalPoint(parent.getTotalPoint() + 500);
        memberRepository.save(parent);

        // 6) 기존 위험도 계산 로직
        double totalRisk = dailySurveyService.calculateRiskScore(dto.getAnswers(), surveys);
        Map<String,Object> result = new HashMap<>();
        result.put("totalRiskScore", totalRisk);
        result.put("categoryScores", dailySurveyService.calculateCategoryRiskScore(dto.getAnswers(), surveys));
        return result;
    }

    // 결과 페이지 라우팅 추가
    @GetMapping("/result")
    public String resultPage() {
        return "dailySurveyResult"; // templates/dailySurveyResult.html
    }
}
