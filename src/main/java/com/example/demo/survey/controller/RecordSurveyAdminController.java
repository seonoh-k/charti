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
@RequestMapping("/api/admin/record-surveys")
@RequiredArgsConstructor
public class RecordSurveyAdminController {

    private final RecordSurveyService recordSurveyService;

    @GetMapping
    public ResponseEntity<?> getPagedSurveys(
            @RequestParam(required = false) String ageGroup,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<RecordSurvey> surveyPage = recordSurveyService.getPagedSurveys(ageGroup, page, size, sortBy, direction);

        List<RecordSurveyResponse> content = surveyPage.getContent()
                .stream()
                .map(RecordSurveyMapper::toResponse)
                .toList();

        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalPages", surveyPage.getTotalPages(),
                "currentPage", surveyPage.getNumber()
        ));
    }


    @PostMapping
    public ResponseEntity<RecordSurveyResponse> createSurvey(@RequestBody RecordSurveyRequest request) {
        RecordSurvey survey = RecordSurveyMapper.toEntity(request);
        recordSurveyService.create(survey);
        return ResponseEntity.ok(RecordSurveyMapper.toResponse(survey));
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long id) {
        recordSurveyService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordSurveyResponse> updateSurvey(
            @PathVariable Long id,
            @RequestBody RecordSurveyRequest request) {

        RecordSurvey survey = recordSurveyService.get(id);
        survey.setAgeGroup(request.getAgeGroup());
        survey.setQuestion(request.getQuestion());
        recordSurveyService.update(survey);

        return ResponseEntity.ok(RecordSurveyMapper.toResponse(survey));
    }
}
