package com.example.demo.survey.service;

import com.example.demo.survey.dto.GroupSurveyRequestDto;
import com.example.demo.survey.dto.GroupSurveyResponseDto;
import com.example.demo.survey.entity.GroupSurvey;
import com.example.demo.survey.repository.GroupSurveyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupSurveyService {

    private final GroupSurveyRepository groupSurveyRepository;

    public List<GroupSurveyResponseDto> getByAgeGroup(String ageGroup) {
        return groupSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup).stream()
                .map(GroupSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GroupSurveyResponseDto> getByCategory(String category) {
        return groupSurveyRepository.findByCategoryAndDeletedFalse(category).stream()
                .map(GroupSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }


    public List<GroupSurveyResponseDto> getByTargetGroup(String targetGroup) {
        return groupSurveyRepository.findByTargetGroupAndDeletedFalse(targetGroup).stream()
                .map(GroupSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void create(GroupSurvey entity) {
        entity.setSurveyDate(LocalDate.now());
        groupSurveyRepository.save(entity);
    }

    @Transactional
    public void update(GroupSurvey entity) {
        GroupSurvey existing = groupSurveyRepository.findById(entity.getId())
                .orElseThrow(() -> new RuntimeException("해당 ID 없음"));
        // 필요한 필드만 복사
        existing.setAgeGroup(entity.getAgeGroup());
        existing.setTargetGroup(entity.getTargetGroup());
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
        entity.markAsDeleted();  // BaseEntity 기능
        groupSurveyRepository.save(entity);
    }


    @Transactional
    public Map<String, Object> evaluate(GroupSurveyRequestDto dto) {
        List<GroupSurvey> surveys = groupSurveyRepository.findByAgeGroupAndDeletedFalse(dto.getAgeGroup());

        if (surveys.size() != dto.getAnswers().size()) {
            throw new IllegalArgumentException("답변 수가 문진 수와 다릅니다.");
        }

        double totalScore = 0;
        Map<String, Double> categoryScores = new HashMap<>();
        Map<String, Integer> categoryWeights = new HashMap<>();

        for (int i = 0; i < surveys.size(); i++) {
            GroupSurvey survey = surveys.get(i);
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
            case 1 -> 0.0;
            case 2 -> 0.25;
            case 3 -> 0.5;
            case 4 -> 0.75;
            case 5 -> 1.0;
            default -> 0.0;
        };
    }


}
