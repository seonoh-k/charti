package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SpecialSurveyRequestDto;
import com.example.demo.survey.dto.SpecialSurveyResponseDto;
import com.example.demo.survey.entity.SpecialSurvey;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.repository.SpecialSurveyRepository;
import com.example.demo.survey.repository.SurveySetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialSurveyService {

    private final SpecialSurveyRepository specialSurveyRepository;
    private final SurveySetRepository surveySetRepository;

    public List<SpecialSurveyResponseDto> getByAgeGroup(String ageGroupDisplayName) {
        AgeGroup ag = AgeGroup.fromValue(ageGroupDisplayName); // displayName 기준
        return specialSurveyRepository.findByAgeGroupAndDeletedFalse(ag).stream()
                .map(SpecialSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SpecialSurveyResponseDto> getBySurveyCategory(String categoryDisplayName) {
        SurveyCategory sc = SurveyCategory.fromValue(categoryDisplayName); // displayName 기준
        return specialSurveyRepository.findByCategoryAndDeletedFalse(sc).stream()
                .map(SpecialSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void create(SpecialSurvey entity, Long surveySetId) {
        specialSurveyRepository.save(entity);
        if (surveySetId != null) {
            SurveySet set = surveySetRepository.findById(surveySetId)
                    .orElseThrow(() -> new RuntimeException("SurveySet ID 없음"));
            set.getSpecialSurveys().add(entity);
            surveySetRepository.save(set);
        }
    }

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

    @Transactional
    public void delete(Long id) {
        SpecialSurvey entity = specialSurveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID 없음"));
        entity.markAsDeleted();
        specialSurveyRepository.save(entity);
    }

    @Transactional
    public Map<String, Object> evaluate(SpecialSurveyRequestDto dto) {
        AgeGroup ag = AgeGroup.fromValue(dto.getAgeGroup()); // displayName 기반 입력
        SurveyCategory sc = SurveyCategory.fromValue(dto.getCategory());

        List<SpecialSurvey> surveys = specialSurveyRepository
                .findByAgeGroupAndDeletedFalse(ag).stream()
                .filter(s -> s.getCategory() == sc)
                .collect(Collectors.toList());

        if (surveys.size() != dto.getAnswers().size()) {
            throw new IllegalArgumentException("답변 수가 문진 수와 다릅니다.");
        }

        double totalScore = 0;
        Map<SurveyCategory, Double> categoryScores = new HashMap<>();
        Map<SurveyCategory, Integer> categoryWeights = new HashMap<>();

        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey survey = surveys.get(i);
            int answer = dto.getAnswers().get(i);
            double multiplier = getMultiplier(answer);
            double score = survey.getWeight() * multiplier;
            totalScore += score;

            SurveyCategory category = survey.getCategory();
            categoryScores.put(category, categoryScores.getOrDefault(category, 0.0) + score);
            categoryWeights.put(category, categoryWeights.getOrDefault(category, 0) + survey.getWeight());
        }

        categoryScores.replaceAll((category, score) ->
                (score / categoryWeights.get(category)) * 100);

        Map<String, Object> result = new HashMap<>();
        result.put("totalRiskScore", totalScore);

        Map<String, Double> convertedCategoryScores = new HashMap<>();
        categoryScores.forEach((category, score) ->
                convertedCategoryScores.put(category.getDisplayName(), score));

        result.put("categoryScores", convertedCategoryScores);

        return result;
    }

    private double getMultiplier(int answer) {
        return switch (answer) {
            case 1 -> 0.0;
            case 2 -> 0.25;
            case 3 -> 0.5;
            case 4 -> 0.75;
            case 5 -> 1.0;
            default -> 0.0;
        };
    }
}
