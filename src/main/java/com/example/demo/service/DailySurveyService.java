package com.example.demo.service;

import com.example.demo.entity.DailySurvey;
import com.example.demo.repository.DailySurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DailySurveyService {

    private final DailySurveyRepository dailySurveyRepository;

    public List<DailySurvey> getSurveysByAgeGroup(String ageGroup) {
        return dailySurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup);
    }

    public List<DailySurvey> getSurveysByAgeAndCategory(String ageGroup, String category) {
        return dailySurveyRepository.findByAgeGroupAndCategoryAndDeletedFalse(ageGroup, category);
    }

    // 응답 기반 위험도 계산
    public double calculateRiskScore(List<Integer> answers, List<DailySurvey> dailySurveyList) {
        if (answers.size() != dailySurveyList.size()) {
            throw new IllegalArgumentException("응답 배열과 설문 데이터 크기가 일치하지 않습니다.");
        }

        double totalScore = 0.0;

        for (int i = 0; i < dailySurveyList.size(); i++) {
            int weight = dailySurveyList.get(i).getWeight();
            int answer = answers.get(i);
            double multiplier = getMultiplier(answer);
            totalScore += weight * multiplier;
        }

        return totalScore;
    }

    public Map<String, Double> calculateCategoryRiskScore(List<Integer> answers, List<DailySurvey> dailySurveyList) {
        Map<String, Double> categoryScores = new HashMap<>();
        Map<String, Integer> categoryWeights = new HashMap<>();

        for (int i = 0; i < dailySurveyList.size(); i++) {
            DailySurvey survey = dailySurveyList.get(i);
            double multiplier = getMultiplier(answers.get(i));
            double weightedScore = survey.getWeight() * multiplier;

            categoryScores.put(
                    survey.getCategory(),
                    categoryScores.getOrDefault(survey.getCategory(), 0.0) + weightedScore
            );
            categoryWeights.put(
                    survey.getCategory(),
                    categoryWeights.getOrDefault(survey.getCategory(), 0) + survey.getWeight()
            );
        }

        // 100점 만점으로 환산
        categoryScores.replaceAll((category, score) ->
                (score / categoryWeights.get(category)) * 100
        );

        return categoryScores;
    }

    // 응답 점수 배수
    private double getMultiplier(int answer) {
        switch (answer) {
            case 1: return 0.0;    // 전혀 아니다
            case 2: return 0.25;   // 거의 그렇지 않다
            case 3: return 0.5;    // 잘 모르겠다
            case 4: return 0.75;   // 약간 그렇다
            case 5: return 1.0;    // 매우 그렇다
            default: return 0.0;
        }
    }


    // 특별 문진 진입 조건
    public boolean needsSpecialSurvey(double categoryScore) {
        return categoryScore >= 60.0; // 위험도 60 이상 시 특별 문진
    }

}
