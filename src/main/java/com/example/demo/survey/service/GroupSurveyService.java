package com.example.demo.survey.service;

import com.example.demo.survey.dto.GroupSurveyRequestDto;
import com.example.demo.survey.dto.GroupSurveyResponseDto;
import com.example.demo.survey.entity.GroupSurvey;
import com.example.demo.survey.repository.GroupSurveyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<GroupSurveyResponseDto> getByTargetGroup(String targetGroup) {
        return groupSurveyRepository.findByTargetGroupAndDeletedFalse(targetGroup).stream()
                .map(GroupSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
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

//    @Transactional
//    public Map<String, Object> evaluate(GroupSurveyRequestDto dto) {
//        List<GroupSurvey> surveys = groupSurveyRepository.findByTargetGroupAndDeletedFalse(dto.getAgeGroup());
//
//        if (surveys.size() != dto.getAnswers().size()) {
//            throw new IllegalArgumentException("답변 수가 문진 수와 다릅니다.");
//        }
//
//        double totalScore = 0;
//        Map<String, Double> categoryScores = new HashMap<>();
//        Map<String, Integer> categoryWeights = new HashMap<>();
//
//        for (int i = 0; i < surveys.size(); i++) {
//            GroupSurvey survey = surveys.get(i);
//            int answer = dto.getAnswers().get(i);
//            double multiplier = getMultiplier(answer);
//            double score = survey.getWeight() * multiplier;
//            totalScore += score;
//
//            categoryScores.put(survey.getCategory(),
//                    categoryScores.getOrDefault(survey.getCategory(), 0.0) + score);
//
//            categoryWeights.put(survey.getCategory(),
//                    categoryWeights.getOrDefault(survey.getCategory(), 0) + survey.getWeight());
//        }
//
//        categoryScores.replaceAll((k, v) -> (v / categoryWeights.get(k)) * 100);
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("totalRiskScore", totalScore);
//        result.put("categoryScores", categoryScores);
//
//        return result;
//    }

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




//    public List<GroupSurvey> getSurveysByAgeGroup(String ageGroup) {
//        return groupSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup.trim());
//    }
//
//    public List<GroupSurvey> getSurveysByTargetGroup(String targetGroup) {
//        return groupSurveyRepository.findByTargetGroupAndDeletedFalse(targetGroup.trim());
//    }

//    public List<GroupSurvey> getSurveysByAgeAndCategory(String ageGroup, String category) {
//        return groupSurveyRepository.findByAgeGroupAndCategoryAndDeletedFalse(ageGroup, category);
//    }

//    public double calculateRiskScore(List<Integer> answers, List<GroupSurvey> groupSurveyList) {
//        if (answers.size() != groupSurveyList.size()) {
//            throw new IllegalArgumentException("응답 배열과 설문 데이터 크기가 일치하지 않습니다.");
//        }
//
//        double totalScore = 0.0;
//
//        for (int i = 0; i < groupSurveyList.size(); i++) {
//            int weight = groupSurveyList.get(i).getWeight();
//            int answer = answers.get(i);
//            double multiplier = getMultiplier(answer);
//            totalScore += weight * multiplier;
//        }
//
//        return totalScore;
//    }
//
//    public Map<String, Double> calculateCategoryRiskScore(List<Integer> answers, List<GroupSurvey> groupSurveyList) {
//        Map<String, Double> categoryScores = new HashMap<>();
//        Map<String, Integer> categoryWeights = new HashMap<>();
//
//        for (int i = 0; i < groupSurveyList.size(); i++) {
//            GroupSurvey survey = groupSurveyList.get(i);
//            double multiplier = getMultiplier(answers.get(i));
//            double weightedScore = survey.getWeight() * multiplier;
//
//            categoryScores.put(
//                    survey.getCategory(),
//                    categoryScores.getOrDefault(survey.getCategory(), 0.0) + weightedScore
//            );
//            categoryWeights.put(
//                    survey.getCategory(),
//                    categoryWeights.getOrDefault(survey.getCategory(), 0) + survey.getWeight()
//            );
//        }
//
//        // 100점 만점으로 환산
//        categoryScores.replaceAll((category, score) ->
//                (score / categoryWeights.get(category)) * 100
//        );
//
//        return categoryScores;
//    }
//
//    // 응답 점수 배수
//    private double getMultiplier(int answer) {
//        switch (answer) {
//            case 1: return 0.0;    // 전혀 아니다
//            case 2: return 0.25;   // 거의 그렇지 않다
//            case 3: return 0.5;    // 잘 모르겠다
//            case 4: return 0.75;   // 약간 그렇다
//            case 5: return 1.0;    // 매우 그렇다
//            default: return 0.0;
//        }
//    }
//
//
//    // 특별 문진 진입 조건
//    public boolean needsSpecialSurvey(double categoryScore) {
//        return categoryScore >= 60.0; // 위험도 60 이상 시 특별 문진
//    }
//
//    // (관리자) 새로운 설문을 저장
//    @Transactional
//    public GroupSurvey save(GroupSurvey survey) {
//        return groupSurveyRepository.save(survey);
//    }
//
//    // (관리자/공통) ID로 설문을 조회
//    public GroupSurvey findById(Long id) {
//        return groupSurveyRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("설문을 찾을 수 없습니다. id=" + id));
//    }
//
//    // (관리자) 설문 삭제
//    @Transactional
//    public void delete(Long id) {
//        groupSurveyRepository.deleteById(id);
//    }
//
//
//    public Object getDistinctCategories() {
//
//        return groupSurveyRepository.findDistinctCategories();
//    }
}
