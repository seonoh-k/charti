package com.example.demo.survey.service;

import com.example.demo.survey.dto.SpecialSurveyRequestDto;
import com.example.demo.survey.dto.SpecialSurveyResponseDto;
import com.example.demo.survey.entity.SpecialSurvey;
import com.example.demo.survey.repository.SpecialSurveyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialSurveyService {

    private final SpecialSurveyRepository specialSurveyRepository;

    // ✅ 연령대 기준 조회
    public List<SpecialSurveyResponseDto> getByAgeGroup(String ageGroup) {
        return specialSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup).stream()
                .map(SpecialSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 카테고리 기준 조회
    public List<SpecialSurveyResponseDto> getByCategory(String category) {
        return specialSurveyRepository.findByCategoryAndDeletedFalse(category).stream()
                .map(SpecialSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }



    // ✅ 생성
    @Transactional
    public void create(SpecialSurvey entity) {
        entity.setSurveyDate(LocalDate.now());
        specialSurveyRepository.save(entity);
    }

    // ✅ 수정
    @Transactional
    public void update(SpecialSurvey entity) {
        SpecialSurvey existing = specialSurveyRepository.findById(entity.getId())
                .orElseThrow(() -> new RuntimeException("해당 ID 없음"));

        existing.setAgeGroup(entity.getAgeGroup());
        existing.setQuestion(entity.getQuestion());
        existing.setCategory(entity.getCategory());
        existing.setWeight(entity.getWeight());
        existing.setAnswer1(entity.getAnswer1());
        existing.setAnswer2(entity.getAnswer2());
        existing.setAnswer3(entity.getAnswer3());
        existing.setAnswer4(entity.getAnswer4());
        existing.setAnswer5(entity.getAnswer5());
        existing.setSelectedAnswer(entity.getSelectedAnswer());
        existing.setCalculatedScore(entity.getCalculatedScore());

        specialSurveyRepository.save(existing);
    }

    // ✅ 삭제
    @Transactional
    public void delete(Long id) {
        SpecialSurvey entity = specialSurveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID 없음"));
        entity.markAsDeleted();
        specialSurveyRepository.save(entity);
    }

    // ✅ 문진 제출 및 평가
    @Transactional
    public Map<String, Object> evaluate(SpecialSurveyRequestDto dto) {
        List<SpecialSurvey> surveys = specialSurveyRepository.findByAgeGroupAndDeletedFalse(dto.getAgeGroup());

        if (surveys.size() != dto.getAnswers().size()) {
            throw new IllegalArgumentException("답변 수가 문진 수와 다릅니다.");
        }

        double totalScore = 0;
        Map<String, Double> categoryScores = new HashMap<>();
        Map<String, Integer> categoryWeights = new HashMap<>();

        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey survey = surveys.get(i);
            int answer = dto.getAnswers().get(i);
            double multiplier = getMultiplier(answer);
            double score = survey.getWeight() * multiplier;
            totalScore += score;

            categoryScores.put(survey.getCategory(),
                    categoryScores.getOrDefault(survey.getCategory(), 0.0) + score);

            categoryWeights.put(survey.getCategory(),
                    categoryWeights.getOrDefault(survey.getCategory(), 0) + survey.getWeight());
        }

        categoryScores.replaceAll((k, v) -> (v / categoryWeights.get(k)) * 100);

        Map<String, Object> result = new HashMap<>();
        result.put("totalRiskScore", totalScore);
        result.put("categoryScores", categoryScores);

        return result;
    }

    private double getMultiplier(int answer) {
        return switch (answer) {
            case 1 -> 1.0; // 전혀 아니다
            case 2 -> 0.75;
            case 3 -> 0.5;
            case 4 -> 0.25;
            case 5 -> 0.0; // 매우 그렇다
            default -> 0.0;
        };
    }
}
