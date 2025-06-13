package com.example.demo.survey.controller;

import com.example.demo.survey.dto.GroupSurveyRequestDto;
import com.example.demo.survey.dto.GroupSurveyResponseDto;
import com.example.demo.survey.service.GroupSurveyService;
import com.example.demo.util.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-surveys")
@RequiredArgsConstructor
public class GroupSurveyController {

    private final GroupSurveyService groupSurveyService;

    // 연령대 기준 조회
    @GetMapping("/by-age/{ageGroup}")
    public ResponseEntity<APIResponse> getSurveysByAgeGroup(@PathVariable String ageGroup) {
        List<GroupSurveyResponseDto> list = groupSurveyService.getByAgeGroup(ageGroup);
        return ResponseEntity.ok(APIResponse.ok(list));
    }

    // 소속 기관 기준 조회
    @GetMapping("/by-target/{targetGroup}")
    public ResponseEntity<APIResponse> getSurveysByTargetGroup(@PathVariable String targetGroup) {
        List<GroupSurveyResponseDto> list = groupSurveyService.getByTargetGroup(targetGroup);
        return ResponseEntity.ok(APIResponse.ok(list));
    }


    @PostMapping("/submit")
    public ResponseEntity<APIResponse> submitSurvey(@RequestBody GroupSurveyRequestDto dto) {
        Map<String, Object> result = groupSurveyService.evaluate(dto);
        return ResponseEntity.ok(APIResponse.ok(result));
    }















}
