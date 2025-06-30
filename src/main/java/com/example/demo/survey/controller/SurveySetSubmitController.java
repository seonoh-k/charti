package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SpecialSurveyRequestDto;
import com.example.demo.survey.dto.SurveySetSubmitRequestDto;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/surveySet")
@RequiredArgsConstructor
@Slf4j
public class SurveySetSubmitController {

    private final SurveySetService surveySetService;
    private final GroupAnswerService groupAnswerService;
    private final SpecialAnswerService specialAnswerService;
    private final GroupSurveyService groupSurveyService;
    private final SpecialSurveyService specialSurveyService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitSurveySet(@RequestBody SurveySetSubmitRequestDto dto) {
        log.info("submitSurveySet: {}", dto);
        SurveySet set = surveySetService.getById(dto.getSetId());
        String type = set.getType();
        log.info("type is {}", type);

        try {
            if ("GROUP".equalsIgnoreCase(type)) {
                groupAnswerService.saveAnswers(dto.getChildId(), dto.getSetId(), dto.getAnswerList());
                Map<String,Object> result = groupSurveyService.evaluate(dto);
                log.info(result.toString());
                return ResponseEntity.ok(result);
            } else if ("SPECIAL".equalsIgnoreCase(type)) {
                AgeGroup ag = AgeGroup.fromValue(dto.getAgeGroup());
                SurveyCategory sc = SurveyCategory.fromValue(dto.getCategory());

                List<SpecialAnswer> savedAnswers = specialAnswerService.saveAndGetAnswers2(dto.getChildId(), ag, sc, dto.getAnswerList());
                Map<String,Object> result = specialSurveyService.evaluate2(dto);

                if((boolean) result.get("needsMatching")) {
                    List<Long> answerIds = savedAnswers.stream().map(SpecialAnswer::getId)
                            .collect(Collectors.toList());
                    result.put("answerIds", answerIds);
                }

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Unknown survey set type: " + type));
            }
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }

//        return ResponseEntity.ok(Map.of("message", "저장 완료"));
    }
}
