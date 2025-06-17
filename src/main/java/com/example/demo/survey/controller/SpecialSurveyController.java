//package com.example.demo.survey.controller;
//
//import com.example.demo.survey.dto.SpecialSurveyRequestDto;
//import com.example.demo.survey.dto.SpecialSurveyResponseDto;
//import com.example.demo.survey.entity.SpecialSurvey;
//import com.example.demo.survey.service.SpecialSurveyService;
//import com.example.demo.util.APIResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/special-surveys")
//@RequiredArgsConstructor
//public class SpecialSurveyController {
//
//    private final SpecialSurveyService specialSurveyService;
//
//    // 1. 연령대 기준 조회
//    @GetMapping("/by-age/{ageGroup}")
//    public List<SpecialSurveyResponseDto> getByAgeGroup(@PathVariable String ageGroup) {
//        return specialSurveyService.getByAgeGroup(ageGroup);
//    }
//
//    // 2. 카테고리 기준 조회
//    @GetMapping
//    public List<SpecialSurveyResponseDto> getByCategory(@RequestParam(required = false) String category) {
//        if (category == null || category.isBlank()) {
//            return List.of();
//        }
//        return specialSurveyService.getByCategory(category);
//    }
//
//    // 3. 설문 등록
//    @PostMapping
//    public String createSurvey(@RequestBody SpecialSurvey dto) {
//        specialSurveyService.create(dto);
//        return "생성 완료";
//    }
//
//    // 4. 설문 수정
//    @PutMapping("/{id}")
//    public String updateSurvey(@PathVariable Long id, @RequestBody SpecialSurvey dto) {
//        dto.setId(id);
//        specialSurveyService.update(dto);
//        return "수정 완료";
//    }
//
//    // 5. 설문 삭제
//    @DeleteMapping("/{id}")
//    public String deleteSurvey(@PathVariable Long id) {
//        specialSurveyService.delete(id);
//        return "삭제 완료";
//    }
//
//    // 6. 제출 및 평가
//    @PostMapping("/submit")
//    public Map<String, Object> submitSurvey(@RequestBody SpecialSurveyRequestDto dto) {
//        return specialSurveyService.evaluate(dto);
//    }
//}
