package com.example.demo.survey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpecialSurveyService {

//    private final SpecialSurveyRepository specialSurveyRepository;
//    public List<SpecialSurveyResponseDto> getByAgeGroup(String ageGroup) {
//        return specialSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup).stream()
//                .map(GroupSurveyResponseDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    public List<GroupSurveyResponseDto> getByTargetGroup(String targetGroup) {
//        return specialSurveyRepository.findByAgeGroupAndDeletedFalse(targetGroup).stream()
//                .map(GroupSurveyResponseDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public Map<String, Object> evaluate(SpecialSurveyRequestDto dto) {
//        List<SpecialSurvey> surveys = specialSurveyRepository.findByAgeGroupAndDeletedFalse(dto.getAgeGroup());
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
//            SpecialSurvey survey = surveys.get(i);
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
//
//    private double getMultiplier(int answer) {
//        return switch (answer) {
//            case 1 -> 0.0;
//            case 2 -> 0.25;
//            case 3 -> 0.5;
//            case 4 -> 0.75;
//            case 5 -> 1.0;
//            default -> 0.0;
//        };
//    }
    
}
