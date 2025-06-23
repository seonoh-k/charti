package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SpecialSurveyRequestDto;
import com.example.demo.survey.dto.SpecialSurveyResponseDto;
import com.example.demo.survey.entity.SpecialSurvey;
import com.example.demo.survey.service.SpecialAnswerService;
import com.example.demo.survey.service.SpecialSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/special-surveys")
@RequiredArgsConstructor
public class SpecialSurveyController {

    private final SpecialSurveyService specialSurveyService;
    private final SpecialAnswerService specialAnswerService;

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
        // 저장
        specialAnswerService.saveAnswers(
                dto.getChildId(),
                AgeGroup.fromValue(dto.getAgeGroup()),
                SurveyCategory.fromValue(dto.getCategory()),
                dto.getAnswers()
        );
        // 평가
        Map<String,Object> result = specialSurveyService.evaluate(dto);
        return ResponseEntity.ok(result);
    }
}