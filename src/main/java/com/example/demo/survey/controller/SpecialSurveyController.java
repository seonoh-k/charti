package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SpecialSurveyRequestDto;
import com.example.demo.survey.dto.SpecialSurveyResponseDto;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.entity.SpecialSurvey;
import com.example.demo.survey.service.SpecialAnswerService;
import com.example.demo.survey.service.SpecialSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/special-surveys")
@RequiredArgsConstructor
public class SpecialSurveyController {

    private final SpecialSurveyService specialSurveyService;
    private final SpecialAnswerService specialAnswerService;

    @GetMapping("/{id}")
    public SpecialSurveyResponseDto getSurveyById(@PathVariable Long id) {
        return specialSurveyService.getSurveyById(id);
    }

    // 1. 연령대 기준 조회
    @GetMapping("/by-age/{ageGroup}")
    public List<SpecialSurveyResponseDto> getSurveysByAgeGroup(@PathVariable String ageGroup) {
        AgeGroup ag;
        try {
            // 먼저 enum.name() 으로 매핑 시도
            ag = AgeGroup.valueOf(ageGroup);
        } catch (IllegalArgumentException e) {
            // 실패하면 displayName 기준 매핑
            ag = AgeGroup.fromValue(ageGroup);
        }

        return specialSurveyService.getByAgeGroup(ag.getDisplayName());
    }

    // 3. 문항 카테고리별 조회
    @GetMapping
    public List<SpecialSurveyResponseDto> getSurveysByCategory(@RequestParam(required = false) String category) {
        if (category == null || category.isBlank()) {
            return List.of();
        }
        return specialSurveyService.getBySurveyCategory(category);
    }

    // 4. 신규 문항 생성 (surveySetId 파라미터로 받음)
    @PostMapping
    public String createSurvey(@RequestBody SpecialSurvey dto, @RequestParam Long surveySetId) {
        specialSurveyService.create(dto, surveySetId);
        return "생성 완료";
    }

    // 5. 기존 문항 수정
    @PutMapping("/{id}")
    public String updateSurvey(@PathVariable Long id, @RequestBody SpecialSurvey dto) {
        dto.setId(id);
        specialSurveyService.update(dto);
        return "수정 완료";
    }

    // 6. 문항 삭제
    @DeleteMapping("/{id}")
    public String deleteSurvey(@PathVariable Long id) {
        specialSurveyService.delete(id);
        return "삭제 완료";
    }

    // 7. 문항 답변 제출
    @PostMapping("/submit")
    public ResponseEntity<Map<String,Object>> submitAndSave(
            @RequestBody SpecialSurveyRequestDto dto) {

        // [수정] 새로 만든 서비스 메소드를 호출하여 저장된 답변 목록을 받음
//        List<SpecialAnswer> savedAnswers = specialAnswerService.saveAndGetAnswers(
//                dto.getChildId(),
//                AgeGroup.fromValue(dto.getAgeGroup()),
//                SurveyCategory.fromValue(dto.getCategory()),
//                dto.getAnswers().stream().map(answer -> answer.get("answerValue")).collect(Collectors.toList()) // answerValue만 추출
//        );

        // 1) enum 파싱
        AgeGroup ag = AgeGroup.fromValue(dto.getAgeGroup());
        SurveyCategory sc = SurveyCategory.fromValue(dto.getCategory());

        // 2) 답변 저장
        List<SpecialAnswer> savedAnswers = specialAnswerService.saveAndGetAnswers(
                dto.getChildId(), ag, sc, dto.getAnswers()
        );

        // 3) 평가 로직
        Map<String,Object> result = specialSurveyService.evaluate(dto);

        // 4) 결과에 childId, enum name, answerIds 담기
        result.put("childId",  dto.getChildId());
        result.put("category", sc.name());
        if ((boolean) result.get("needsMatching")) {
            List<Long> answerIds = savedAnswers.stream()
                    .map(SpecialAnswer::getId)
                    .toList();
            result.put("answerIds", answerIds);
        }

        // [수정] 매칭이 필요할 때, 저장된 답변들의 ID 목록을 결과에 추가
//        if ((boolean) result.get("needsMatching")) {
//            List<Long> answerIds = savedAnswers.stream()
//                    .map(SpecialAnswer::getId)
//                    .collect(Collectors.toList());
//            result.put("answerIds", answerIds);
//        }

        return ResponseEntity.ok(result);
    }


}