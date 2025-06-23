package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.dto.GroupSurveyRequestDto;
import com.example.demo.survey.dto.GroupSurveyResponseDto;
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
//        existing.setWeight(entity.getWeight());
        existing.setAnswer1(entity.getAnswer1());
        existing.setAnswer2(entity.getAnswer2());
        existing.setAnswer3(entity.getAnswer3());
        existing.setAnswer4(entity.getAnswer4());
        existing.setAnswer5(entity.getAnswer5());
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
    public Map<String,Object> evaluate(GroupSurveyRequestDto dto) {
        AgeGroup ag = AgeGroup.fromValue(dto.getAgeGroup());
        String tgt = dto.getTargetGroup();
        boolean allTargets = (tgt == null || tgt.isBlank() || "all".equalsIgnoreCase(tgt));
        TargetGroup tg = allTargets
                ? null
                : TargetGroup.fromValue(tgt);

        // 1) DB에서 설문 문항 조회
        List<GroupSurvey> surveys = groupSurveyRepository.findByAgeGroupAndDeletedFalse(ag);

        // 2) 대상 그룹 필터링
        if (!allTargets) {
            surveys = surveys.stream()
                    .filter(gs -> gs.getTargetGroup().filter(enumTg -> enumTg == tg).isPresent())
                    .toList();
        }

        // 3) 답변 개수 검증
        if (surveys.size() != dto.getAnswers().size()) {
            throw new IllegalArgumentException(
                    "답변 수가 문진 수와 다릅니다. 서버측 설문 "
                            + surveys.size() + "개, 클라이언트측 응답 "
                            + dto.getAnswers().size() + "개"
            );
        }

        // [수정] '답변 비율의 평균'을 계산하기 위한 Map만 사용
        Map<SurveyCategory, Double> categoryMultiplierSum = new HashMap<>();
        Map<SurveyCategory, Integer> categoryQuestionCount = new HashMap<>();

        // [수정] 루프에서 weight 관련 계산 모두 제거
        for (int i = 0; i < surveys.size(); i++) {
            GroupSurvey survey = surveys.get(i);
            int answer = dto.getAnswers().get(i);
            double multiplier = getMultiplier(answer);

            SurveyCategory cat = survey.getCategory();

            // 카테고리별 답변 비율(multiplier)의 합과 문항 수를 기록
            categoryMultiplierSum.merge(cat, multiplier, Double::sum);
            categoryQuestionCount.merge(cat, 1, Integer::sum);
        }

        // [수정] 최종 점수 계산 및 특별 설문 필요 여부 동시 확인
        Map<String, Double> finalCategoryScores = new HashMap<>();
        boolean needsSpecialSurvey = false;
        List<String> specialCategories = new ArrayList<>();

        for (SurveyCategory cat : categoryMultiplierSum.keySet()) {
            double sumOfMultipliers = categoryMultiplierSum.get(cat);
            int countOfQuestions = categoryQuestionCount.get(cat);

            // 카테고리별 '답변 위험도 평균 퍼센트' 계산
            double averageRiskPercentage = (countOfQuestions > 0)
                    ? (sumOfMultipliers / countOfQuestions) * 100
                    : 0.0;

            // 최종 결과 맵에 저장
            finalCategoryScores.put(cat.getDisplayName(), averageRiskPercentage);

            // 평균 위험도가 60% 이상인지 확인
            if (needsSpecialSurvey(averageRiskPercentage)) {
                needsSpecialSurvey = true;
                specialCategories.add(cat.name());
            }
        }

        // [수정] 최종 결과 Map 구성
        Map<String,Object> result = new HashMap<>();
        result.put("categoryScores", finalCategoryScores); // weight와 무관한 평균 위험도 점수

        // 특별 설문 관련 정보 추가
        result.put("needsSpecialSurvey", needsSpecialSurvey);
        if (needsSpecialSurvey) {
            result.put("childId", dto.getChildId());
            result.put("ageGroup", dto.getAgeGroup());
            result.put("specialCategories", specialCategories);
        }

        return result;
    }

    private double getMultiplier(int answer) {
        return switch (answer) {
            case 1 -> 1.0;  // 100%
            case 2 -> 0.75; // 75%
            case 3 -> 0.5;  // 50%
            case 4 -> 0.25; // 25%
            case 5 -> 0.0;  // 0%
            default -> 0.0;
        };
    }

    public boolean needsSpecialSurvey(double categoryScore) {
        return categoryScore >= 60.0; // 위험도 60% 이상 시 특별 문진
    }
}