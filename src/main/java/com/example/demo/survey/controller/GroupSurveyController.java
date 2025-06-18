package com.example.demo.survey.controller;

import com.example.demo.survey.dto.GroupSurveyRequestDto;
import com.example.demo.survey.dto.GroupSurveyResponseDto;
import com.example.demo.survey.entity.GroupSurvey;
import com.example.demo.survey.service.GroupSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-surveys")
@RequiredArgsConstructor
public class GroupSurveyController {

//    private final GroupSurveyService groupSurveyService;
//
//    // 1. 연령대 기준 조회
//    @GetMapping("/by-age/{ageGroup}")
//    public List<GroupSurveyResponseDto> getSurveysByAgeGroup(@PathVariable String ageGroup) {
//        return groupSurveyService.getByAgeGroup(ageGroup);
//    }
//
//
//    // 2. 대상 그룹(유치원, 어린이집 등) 기준 조회
//    @GetMapping("/by-target/{targetGroup}")
//    public List<GroupSurveyResponseDto> getSurveysByTargetGroup(@PathVariable String targetGroup) {
//        return groupSurveyService.getByTargetGroup(targetGroup);
//    }
//
//    // 3. 문항 카테고리별 조회
//    @GetMapping
//    public List<GroupSurveyResponseDto> getSurveysByCategory(@RequestParam(required = false) String category) {
//        if (category == null || category.isBlank()) {
//            return List.of();
//        }
//        return groupSurveyService.getByCategory(category);
//    }
//
//    // 4. 신규문항 생성
//    @PostMapping
//    public String createSurvey(@RequestBody GroupSurvey dto) {
//        groupSurveyService.create(dto);
//        return "생성 완료";
//    }
//
//    // 5. 기존 문항 업데이트
//    @PutMapping("/{id}")
//    public String updateSurvey(@PathVariable Long id, @RequestBody GroupSurvey dto) {
//        dto.setId(id);
//        groupSurveyService.update(dto);
//        return "수정 완료";
//    }
//
//    // 6. 문항 삭제(ID입력 시 soft-delete방식 처리)
//    @DeleteMapping("/{id}")
//    public String deleteSurvey(@PathVariable Long id) {
//        groupSurveyService.delete(id);
//        return "삭제 완료";
//    }
//
//    // 7. 문항 답변 제출
//    @PostMapping("/submit")
//    public Map<String, Object> submitSurvey(@RequestBody GroupSurveyRequestDto dto) {
//        return groupSurveyService.evaluate(dto);
//    }


}
