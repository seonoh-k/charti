package com.example.demo.survey.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/special-surveys")
@RequiredArgsConstructor
public class SpecialSurveyController {

//    private final SpecialSurveyService specialSurveyService;
//
//    // 연령대 기준 조회
//    @GetMapping("/by-age/{ageGroup}")
//    public ResponseEntity<APIResponse> getSurveysByAgeGroup(@PathVariable String ageGroup) {
//        List<GroupSurveyResponseDto> list = specialSurveyService.getByAgeGroup(ageGroup);
//        return ResponseEntity.ok(APIResponse.ok(list));
//    }
//
//    // 소속 기관 기준 조회
//    @GetMapping("/by-target/{targetGroup}")
//    public ResponseEntity<APIResponse> getSurveysByTargetGroup(@PathVariable String targetGroup) {
//        List<GroupSurveyResponseDto> list = specialSurveyService.getByTargetGroup(targetGroup);
//        return ResponseEntity.ok(APIResponse.ok(list));
//    }
//
//
//    @PostMapping("/submit")
//    public ResponseEntity<APIResponse> submitSurvey(@RequestBody SpecialSurveyRequestDto dto) {
//        Map<String, Object> result = specialSurveyService.evaluate(dto);
//        return ResponseEntity.ok(APIResponse.ok(result));
//    }

}
