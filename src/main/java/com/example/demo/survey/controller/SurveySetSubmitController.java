package com.example.demo.survey.controller;

import com.example.demo.survey.dto.SurveySetSubmitRequestDto;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SpecialAnswerService;
import com.example.demo.survey.service.GroupAnswerService;
import com.example.demo.survey.service.SurveySetService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/surveySet")
@RequiredArgsConstructor
public class SurveySetSubmitController {

    private final SurveySetService surveySetService;
    private final GroupAnswerService groupAnswerService;
    private final SpecialAnswerService specialAnswerService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitSurveySet(@RequestBody SurveySetSubmitRequestDto dto) {
        SurveySet set = surveySetService.getById(dto.getSetId());
        String type = set.getType();

        try {
            if ("GROUP".equalsIgnoreCase(type)) {
                groupAnswerService.saveAnswers(dto.getChildId(), dto.getSetId(), dto.getAnswers());
            } else if ("SPECIAL".equalsIgnoreCase(type)) {
                specialAnswerService.saveAnswers(dto.getChildId(), dto.getSetId(), dto.getAnswers());
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Unknown survey set type: " + type));
            }
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", "저장 완료"));
    }
}
