package com.example.demo.survey.controller;

import com.example.demo.survey.dto.RecordSurveyRequest;
import com.example.demo.survey.dto.RecordSurveyResponse;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.mapper.RecordSurveyMapper;
import com.example.demo.survey.service.RecordSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/record-surveys")
@RequiredArgsConstructor
public class RecordSurveyController {

    private final RecordSurveyService recordSurveyService;


    @GetMapping
    public ResponseEntity<List<RecordSurveyResponse>> getAllForUser(
            @RequestParam(required = false) String ageGroup) {
        List<RecordSurvey> surveys = recordSurveyService.getByAgeGroup(ageGroup);
        return ResponseEntity.ok(
                surveys.stream().map(RecordSurveyMapper::toResponse).toList()
        );
    }

}
