package com.example.demo.survey.controller;

import com.example.demo.survey.dto.RecordSurveyRequest;
import com.example.demo.survey.dto.RecordSurveyResponse;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.mapper.RecordSurveyMapper;
import com.example.demo.survey.service.RecordSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/record-surveys")
@RequiredArgsConstructor
public class RecordSurveyController {

    private final RecordSurveyService recordSurveyService;

    @PostMapping
    public ResponseEntity<RecordSurveyResponse> createSurvey(@RequestBody RecordSurveyRequest request) {
        RecordSurvey survey = RecordSurveyMapper.toEntity(request);
        recordSurveyService.create(survey);
        return ResponseEntity.ok(RecordSurveyMapper.toResponse(survey));
    }

    @GetMapping
    public ResponseEntity<List<RecordSurveyResponse>> getAll() {
        List<RecordSurvey> surveys = recordSurveyService.getAllActiveSurveys();
        return ResponseEntity.ok(
                surveys.stream().map(RecordSurveyMapper::toResponse).toList()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long id) {
        recordSurveyService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
