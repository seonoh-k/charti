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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GroupSurveyService {

    private final GroupSurveyRepository groupSurveyRepository;
    private final SurveySetRepository surveySetRepository;

    public GroupSurveyResponseDto getSurveyById(Long id) {
        return groupSurveyRepository.findById(id)
                .map(GroupSurveyResponseDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 문항을 찾을 수 없습니다: " + id));
    }


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

        Map<SurveyCategory, Double> categoryMultiplierSum = new HashMap<>();
        Map<SurveyCategory, Integer> categoryQuestionCount = new HashMap<>();

        for (int i = 0; i < surveys.size(); i++) {
            GroupSurvey survey = surveys.get(i);
            int answer = dto.getAnswers().get(i);

            // [수정] 엔티티 수정 없이 서비스 내에서 직접 선택지 개수 계산
            long totalOptions = Stream.of(survey.getAnswer1(), survey.getAnswer2(), survey.getAnswer3(), survey.getAnswer4(), survey.getAnswer5())
                    .filter(ans -> ans != null && !ans.isBlank())
                    .count();

            // [수정] 수정된 getMultiplier 호출 (long을 int로 캐스팅)
            double multiplier = getMultiplier(answer, (int)totalOptions);

            SurveyCategory cat = survey.getCategory();

            categoryMultiplierSum.merge(cat, multiplier, Double::sum);
            categoryQuestionCount.merge(cat, 1, Integer::sum);
        }

        Map<String, Double> finalCategoryScores = new HashMap<>();
        boolean needsSpecialSurvey = false;
        List<String> specialCategories = new ArrayList<>();

        for (SurveyCategory cat : categoryMultiplierSum.keySet()) {
            double sumOfMultipliers = categoryMultiplierSum.get(cat);
            int countOfQuestions = categoryQuestionCount.get(cat);

            double averageRiskPercentage = (countOfQuestions > 0)
                    ? (sumOfMultipliers / countOfQuestions) * 100
                    : 0.0;

            finalCategoryScores.put(cat.getDisplayName(), averageRiskPercentage);

            if (needsSpecialSurvey(averageRiskPercentage)) {
                needsSpecialSurvey = true;
                specialCategories.add(cat.name());
            }
        }

        Map<String,Object> result = new HashMap<>();
        result.put("categoryScores", finalCategoryScores);

        result.put("needsSpecialSurvey", needsSpecialSurvey);
        if (needsSpecialSurvey) {
            result.put("childId", dto.getChildId());
            result.put("ageGroup", dto.getAgeGroup());
            result.put("specialCategories", specialCategories);
        }

        return result;
    }

    /**
     * [수정] 답변 값과 '총 선택지 개수'에 따라 위험도 계수를 반환하는 메소드
     * @param answer 선택한 답변 번호 (1, 2, 3...)
     * @param totalOptions 해당 질문의 총 선택지 개수 (2, 3, 5 등)
     * @return 위험도 계수 (0.0 ~ 1.0)
     */
    private double getMultiplier(int answer, int totalOptions) {
        switch (totalOptions) {
            case 5: // 5지선다
                return switch (answer) {
                    case 1 -> 1.0;  // 100%
                    case 2 -> 0.75; // 75%
                    case 3 -> 0.5;  // 50%
                    case 4 -> 0.25; // 25%
                    case 5 -> 0.0;  // 0%
                    default -> 0.0;
                };
            case 3: // 3지선다
                return switch (answer) {
                    case 1 -> 1.0; // 100%
                    case 2 -> 0.5; // 50%
                    case 3 -> 0.0; // 0%
                    default -> 0.0;
                };
            case 2: // 2지선다 (역계산)
                return switch (answer) {
                    case 1 -> 0.0;  // 0%
                    case 2 -> 1.0;  // 100%
                    default -> 0.0;
                };
            default: // 그 외의 경우는 0점 처리
                return 0.0;
        }
    }

    public boolean needsSpecialSurvey(double categoryScore) {
        return categoryScore >= 60.0; // 위험도 60% 이상 시 특별 문진
    }
}
