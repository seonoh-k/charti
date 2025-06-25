// com.example.demo.survey.service.SpecialSurveyService.java
package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SpecialSurveyRequestDto;
import com.example.demo.survey.dto.SpecialSurveyResponseDto;
import com.example.demo.survey.entity.SpecialSurvey;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.repository.SpecialSurveyRepository;
import com.example.demo.survey.repository.SurveySetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpecialSurveyService {

    private final SpecialSurveyRepository specialSurveyRepository;
    private final SurveySetRepository surveySetRepository;

    @Transactional(readOnly = true)
    public Map<String,Object> evaluate(SpecialSurveyRequestDto dto) {
        AgeGroup ag = AgeGroup.fromValue(dto.getAgeGroup());
        SurveyCategory sc = SurveyCategory.fromValue(dto.getCategory());

        // 1) DB에서 연령대와 카테고리 기준으로 설문 문항 조회
        List<SpecialSurvey> surveys = specialSurveyRepository.findByAgeGroupAndCategoryAndDeletedFalse(ag, sc);

        // 2) 답변 개수 검증
        if (surveys.size() != dto.getAnswers().size()) {
            throw new IllegalArgumentException(
                    "답변 수가 문진 수와 다릅니다. 서버측 설문 "
                            + surveys.size() + "개, 클라이언트측 응답 "
                            + dto.getAnswers().size() + "개"
            );
        }

        // 3) '답변 비율의 평균'을 계산하기 위한 Map 초기화
        Map<SurveyCategory, Double> categoryMultiplierSum = new HashMap<>();
        Map<SurveyCategory, Integer> categoryQuestionCount = new HashMap<>();

        // 4) 루프에서 weight 관련 계산 모두 제거
        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey survey = surveys.get(i);

            // [핵심 수정] DTO의 List<Map> 구조에 맞게 'answerValue' 키로 값을 가져오도록 변경
            int answer = dto.getAnswers().get(i).get("answerValue");

            long totalOptions = Stream.of(survey.getAnswer1(), survey.getAnswer2(), survey.getAnswer3(), survey.getAnswer4(), survey.getAnswer5())
                    .filter(ans -> ans != null && !ans.isBlank())
                    .count();
            double multiplier = getMultiplier(answer, (int)totalOptions);

            SurveyCategory cat = survey.getCategory();

            // 카테고리별 답변 비율(multiplier)의 합과 문항 수를 기록
            categoryMultiplierSum.merge(cat, multiplier, Double::sum);
            categoryQuestionCount.merge(cat, 1, Integer::sum);
        }

        // 5) 최종 점수 계산 및 매칭 필요 여부 동시 확인
        Map<String, Double> finalCategoryScores = new HashMap<>();
        boolean needsMatching = false; // 플래그 이름 변경

        for (SurveyCategory cat : categoryMultiplierSum.keySet()) {
            double sumOfMultipliers = categoryMultiplierSum.get(cat);
            int countOfQuestions = categoryQuestionCount.get(cat);

            // 카테고리별 '답변 위험도 평균 퍼센트' 계산
            double averageRiskPercentage = (countOfQuestions > 0)
                    ? (sumOfMultipliers / countOfQuestions) * 100
                    : 0.0;

            finalCategoryScores.put(cat.getDisplayName(), averageRiskPercentage);

            // 평균 위험도가 60% 이상인지 확인
            if (needsMatching(averageRiskPercentage)) {
                needsMatching = true;
            }
        }

        // 6) 최종 결과 Map 구성
        Map<String,Object> result = new HashMap<>();
        result.put("categoryScores", finalCategoryScores);
        result.put("needsMatching", needsMatching); // 키 이름 변경

        // 매칭이 필요할 경우 관련 정보 추가 (추후 매칭 테이블 저장 시 사용)
        if (needsMatching) {
            result.put("childId", dto.getChildId());
            // [수정] category는 ENUM의 name()을 반환하도록 통일
            result.put("category", sc.name());
        }

        return result;
    }

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

    // 신규 메소드 추가
    public boolean needsMatching(double categoryScore) {
        return categoryScore >= 60.0; // 60% 이상 시 매칭 필요
    }

    public SpecialSurveyResponseDto getSurveyById(Long id) {
        return specialSurveyRepository.findById(id)
                .map(SpecialSurveyResponseDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 문항을 찾을 수 없습니다: " + id));
    }

    // (이하 기존 코드 유지)
    public List<SpecialSurveyResponseDto> getByAgeGroup(String ageGroupDisplayName) {
        AgeGroup ag = AgeGroup.fromValue(ageGroupDisplayName);
        return specialSurveyRepository.findByAgeGroupAndDeletedFalse(ag).stream()
                .map(SpecialSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SpecialSurveyResponseDto> getBySurveyCategory(String categoryDisplayName) {
        SurveyCategory sc = SurveyCategory.fromValue(categoryDisplayName);
        return specialSurveyRepository.findByCategoryAndDeletedFalse(sc).stream()
                .map(SpecialSurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void create(SpecialSurvey entity, Long surveySetId) {
        specialSurveyRepository.save(entity);
        if (surveySetId != null) {
            SurveySet set = surveySetRepository.findById(surveySetId)
                    .orElseThrow(() -> new EntityNotFoundException("SurveySet ID 없음: " + surveySetId));
            set.getSpecialSurveys().add(entity);
            surveySetRepository.save(set);
        }
    }

    @Transactional
    public void update(SpecialSurvey entity) {
        SpecialSurvey existing = specialSurveyRepository.findById(entity.getId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID 없음: " + entity.getId()));
        existing.setAgeGroup(entity.getAgeGroup());
        existing.setQuestion(entity.getQuestion());
        existing.setCategory(entity.getCategory());
        existing.setAnswer1(entity.getAnswer1());
        existing.setAnswer2(entity.getAnswer2());
        existing.setAnswer3(entity.getAnswer3());
        existing.setAnswer4(entity.getAnswer4());
        existing.setAnswer5(entity.getAnswer5());
        specialSurveyRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        SpecialSurvey entity = specialSurveyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID 없음: " + id));
        entity.markAsDeleted();
        specialSurveyRepository.save(entity);
    }
}