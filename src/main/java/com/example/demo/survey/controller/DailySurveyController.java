package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SurveyRequestDto;
import com.example.demo.survey.entity.DailyAnswer;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.repository.DailyAnswerRepository;
import com.example.demo.survey.service.DailyAnswerService;
import com.example.demo.survey.service.DailySurveyService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.RiskCategory;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.RiskCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final DailyAnswerService dailyAnswerService;
    private final DailyAnswerRepository answerRepository;
    private final ChildService childService;
    private final MemberRepository memberRepository;
    private final RiskCategoryService riskCategoryService;

    @GetMapping("/{ageGroup}")
    @ResponseBody
    public List<DailySurvey> getSurveyListByAgeGroup(@PathVariable AgeGroup ageGroup) {
        return dailySurveyService.getSurveysByAgeGroup(ageGroup);
    }

    // 오늘 이미 문진했는지 체크
    @GetMapping("/check/{childId}")
    @ResponseBody
    public ResponseEntity<Void> checkAnsweredToday(@PathVariable Long childId) {
        Child child = childService.findById(childId);
        boolean answered = dailyAnswerService.hasAnsweredToday(child);
        if (answered) {
            // HTTP 409 Conflict 로 이미 작성됨을 알림
            return ResponseEntity.status(409).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/submit",
            consumes = "application/json; charset=UTF-8",
            produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<?> submitSurvey(@RequestBody SurveyRequestDto dto) {
        // 1) 자녀 조회
        Child child = childService.findById(dto.getChildId());

        // 하루 한 번만 체크
        if (dailyAnswerService.hasAnsweredToday(child)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "오늘 이미 데일리 문진을 작성했습니다."));
        }

        // 2) 설문 목록 조회 & 검증 (기존 로직 그대로)
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

        // 3) 답변 저장 (기존 로직)
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
            answerRepository.save(da);
        }

        // 4) 포인트 적립 (기존 로직)
        Member parent = child.getParent();
        if (parent.getTotalPoint() == null) {
            parent.setTotalPoint(0);
        }
        parent.setTotalPoint(parent.getTotalPoint() + 500);
        memberRepository.save(parent);

        // 5) 위험도 계산 및 결과 반환 (기존 로직)
        double totalRisk = dailySurveyService.calculateRiskScore(dto.getAnswers(), surveys);
        Map<String,Object> result = new HashMap<>();
        result.put("totalRiskScore", totalRisk);
        result.put("categoryScores", dailySurveyService.calculateCategoryRiskScore(dto.getAnswers(), surveys));

//        boolean needsSpecial = result.get("categoryScores") != null
//                && ((Map<SurveyCategory,Double>)result.get("categoryScores")).values().stream()
//                .anyMatch(score -> score >= 60.0);
        boolean needsSpecial = false;

        Map<SurveyCategory, Double> categoryScores = (Map<SurveyCategory, Double>) result.get("categoryScores");
        for (SurveyCategory category : categoryScores.keySet()) {
            Double score = categoryScores.get(category);
            if(score >= 60.0) {
                needsSpecial = true;

                RiskCategory riskCategory = new RiskCategory();
                riskCategory.setChild(child);
                riskCategory.setSurveyCategory(category);
                RiskCategory riskCategory1 = riskCategoryService.createRiskCategory(riskCategory);
                child.setRiskGroup(true);
                child.getRiskCategories().add(riskCategory1);
                childService.update(child);
            }
        }


        result.put("needsSpecialSurvey", needsSpecial);

        return ResponseEntity.ok(result);
    }

    // 결과 페이지 라우팅 추가
    @GetMapping("/result")
    public String resultPage() {
        return "dailySurveyResult"; // templates/dailySurveyResult.html
    }
}
