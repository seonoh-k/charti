package com.example.demo.survey.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.dto.SurveySetDTO;
import com.example.demo.survey.entity.BaseSurvey;
import com.example.demo.survey.service.SurveySetService;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyApiController {
    private final SurveySetService service;

    /**
     * 문진 목록 조회 API
     *
     * @param type        "GROUP" 또는 "SPECIAL"
     * @param age         AgeGroup enum (ALL 포함)
     * @param category    SurveyCategory enum (ALL 포함)
     * @param targetGroup TargetGroup enum (ALL 포함, GROUP일 때만 적용)
     * @return BaseSurvey 타입의 문진 리스트
     */
    @GetMapping
    public List<? extends BaseSurvey> getSurveys(
            @RequestParam String type,
            @RequestParam(defaultValue = "ALL") AgeGroup age,
            @RequestParam(defaultValue = "ALL") SurveyCategory category,
            @RequestParam(defaultValue = "ALL") String targetGroup
    ) {

        TargetGroup tg = TargetGroup.fromValue(targetGroup);

        if ("SPECIAL".equalsIgnoreCase(type)) {
            // 특별 문진은 targetGroup 무시
            return service.allSpecial(age, category);
        } else {
            // 그룹 문진만 targetGroup 필터 적용
            return service.allGroup(age, category, tg);
        }
    }

    @PostMapping("/findSet")
    public ResponseEntity<ApiResponse> getSurveySet(@RequestParam(defaultValue = "ALL") AgeGroup age,
                                                    @RequestParam(defaultValue = "ALL") SurveyCategory category) {

        SurveySetDTO set = service.getSurveySet(age, category);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK, set));
    }
}
