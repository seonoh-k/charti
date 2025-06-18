package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.dto.GroupSurveyRequestDto;
import com.example.demo.survey.dto.GroupSurveyResponseDto;
import com.example.demo.survey.dto.GroupSurveyWithSetDto;
import com.example.demo.survey.entity.GroupSurvey;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.repository.GroupSurveyRepository;
import com.example.demo.survey.repository.SurveySetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupSurveyService {

    private final GroupSurveyRepository groupSurveyRepository;
    private final SurveySetRepository surveySetRepository;

    public List<GroupSurveyResponseDto> getByAgeGroup(String ageGroup) {
        AgeGroup ag = AgeGroup.fromValue(ageGroup);
        return groupSurveyRepository.findByAgeGroupAndDeletedFalse(ag).stream()
                .map(GroupSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GroupSurveyResponseDto> getBySurveyCategory(String category) {
        SurveyCategory sc = SurveyCategory.fromValue(category);
        return groupSurveyRepository.findByCategoryAndDeletedFalse(sc).stream()
                .map(GroupSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GroupSurveyResponseDto> getByTargetGroup(String targetGroup) {
        if ("all".equalsIgnoreCase(targetGroup)) {
            return groupSurveyRepository.findAll().stream()
                    .filter(gs -> !gs.isDeleted())
                    .map(GroupSurveyResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }

        TargetGroup tg = TargetGroup.fromValue(targetGroup);
        return groupSurveyRepository.findByTargetGroupPrefix(tg.getDisplayName()).stream()
                .map(GroupSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void create(GroupSurvey entity, Long surveySetId) {
        groupSurveyRepository.save(entity);
        if (surveySetId != null) {
            SurveySet set = surveySetRepository.findById(surveySetId)
                    .orElseThrow(() -> new RuntimeException("SurveySet ID 없음"));
            set.getGroupSurveys().add(entity);
            surveySetRepository.save(set);
        }
    }

    @Transactional
    public void update(GroupSurvey entity) {
        GroupSurvey existing = groupSurveyRepository.findById(entity.getId())
                .orElseThrow(() -> new RuntimeException("해당 ID 없음"));
        existing.setAgeGroup(entity.getAgeGroup());
        existing.setTargetGroup(entity.getTargetGroup().orElse(null));
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
        groupSurveyRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        GroupSurvey entity = groupSurveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID 없음"));
        entity.markAsDeleted();
        groupSurveyRepository.save(entity);
    }

    @Transactional
    public Map<String, Object> evaluate(GroupSurveyRequestDto dto) {
        AgeGroup ag = AgeGroup.fromValue(dto.getAgeGroup());
        List<GroupSurvey> surveys = groupSurveyRepository.findByAgeGroupAndDeletedFalse(ag);

        if (surveys.size() != dto.getAnswers().size()) {
            throw new IllegalArgumentException("답변 수가 문진 수와 다릅니다.");
        }

        double totalScore = 0;
        Map<SurveyCategory, Double> categoryScores = new HashMap<>();
        Map<SurveyCategory, Integer> categoryWeights = new HashMap<>();

        for (int i = 0; i < surveys.size(); i++) {
            GroupSurvey survey = surveys.get(i);
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
