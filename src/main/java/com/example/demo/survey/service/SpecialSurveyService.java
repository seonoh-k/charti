// com.example.demo.survey.service.SpecialSurveyService.java
package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SpecialSurveyRequestDto;
import com.example.demo.survey.dto.SpecialSurveyResponseDto;
import com.example.demo.survey.dto.SurveySetSubmitRequestDto;
import com.example.demo.survey.entity.GroupSurvey;
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

        // 4) 각 문항별 multiplier 합산
        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey survey = surveys.get(i);
            SpecialSurveyRequestDto.AnswerDto ansDto = dto.getAnswers().get(i);

            // 전체 선택지 개수
            long totalOptions = Stream.of(
                            survey.getAnswer1(), survey.getAnswer2(),
                            survey.getAnswer3(), survey.getAnswer4(),
                            survey.getAnswer5()
                    )
                    .filter(opt -> opt != null && !opt.isBlank())
                    .count();

            // 클라이언트가 보낸 텍스트 응답
            String answerText = ansDto.getAnswerText();

            // 옵션 리스트로 변환
            List<String> options = Stream.of(
                            survey.getAnswer1(), survey.getAnswer2(),
                            survey.getAnswer3(), survey.getAnswer4(),
                            survey.getAnswer5()
                    )
                    .filter(opt -> opt != null && !opt.isBlank())
                    .toList();

            // 텍스트가 옵션의 몇 번째인지 찾기 (0-based)
            int idx = options.indexOf(answerText);
            if (idx < 0) {
                throw new IllegalArgumentException(
                        "응답값이 예상된 선택지에 없습니다: " + answerText
                );
            }
            int answerIndex = idx + 1; // 1-based index

            // multiplier 계산
            double multiplier = getMultiplier(answerIndex, (int) totalOptions);
            categoryMultiplierSum.merge(survey.getCategory(), multiplier, Double::sum);
            categoryQuestionCount.merge(survey.getCategory(), 1, Integer::sum);
        }

        // 5) 최종 점수 계산 및 매칭 필요 여부 확인
        Map<String, Double> finalCategoryScores = new HashMap<>();
        boolean needsMatching = false;

        for (SurveyCategory cat : categoryMultiplierSum.keySet()) {
            double sumOfMultipliers = categoryMultiplierSum.get(cat);
            int count = categoryQuestionCount.getOrDefault(cat, 0);

            double avgPercent = (count > 0 ? (sumOfMultipliers / count) * 100 : 0.0);
            finalCategoryScores.put(cat.getDisplayName(), avgPercent);
            if (needsMatching(avgPercent)) {
                needsMatching = true;
            }
        }

        // 6) 결과 구성
        Map<String,Object> result = new HashMap<>();
        result.put("categoryScores", finalCategoryScores);
        result.put("needsMatching", needsMatching);

        if (needsMatching) {
            result.put("childId", dto.getChildId());
            result.put("category", sc.name());
            List<Long> answerIds = dto.getAnswers().stream()
                    .map(SpecialSurveyRequestDto.AnswerDto::getSurveyId)
                    .collect(Collectors.toList());
            result.put("answerIds", answerIds);
        }

        return result;
    }


    @jakarta.transaction.Transactional
    public Map<String,Object> evaluate2(SurveySetSubmitRequestDto dto) {
        AgeGroup ag = AgeGroup.fromValue(dto.getAgeGroup());
        SurveyCategory sc = SurveyCategory.fromValue(dto.getCategory());

        // 1) DB에서 설문 문항 조회
        List<SpecialSurvey> surveys = specialSurveyRepository.findBySurveySetId(dto.getSetId());

        // 3) 답변 개수 검증
        if (surveys.size() != dto.getAnswerList().size()) {
            throw new IllegalArgumentException(
                    "답변 수가 문진 수와 다릅니다. 서버측 설문 "
                            + surveys.size() + "개, 클라이언트측 응답 "
                            + dto.getAnswerList().size() + "개"
            );
        }

        Map<SurveyCategory, Double> categoryMultiplierSum = new HashMap<>();
        Map<SurveyCategory, Integer> categoryQuestionCount = new HashMap<>();

        Map<Long, Integer> answerMap = dto.getAnswerList().stream()
                .collect(Collectors.toMap(SurveySetSubmitRequestDto.AnswerDto::getSurveyId,
                        SurveySetSubmitRequestDto.AnswerDto::getAnswerValue));

        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey survey = surveys.get(i);
            int answer = answerMap.get(survey.getId());

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

        // 5) 최종 점수 계산 및 매칭 필요 여부 확인
        Map<String, Double> finalCategoryScores = new HashMap<>();
        boolean needsMatching = false;

        for (SurveyCategory cat : categoryMultiplierSum.keySet()) {
            double sumOfMultipliers = categoryMultiplierSum.get(cat);
            int count = categoryQuestionCount.getOrDefault(cat, 0);

            double avgPercent = (count > 0 ? (sumOfMultipliers / count) * 100 : 0.0);
            finalCategoryScores.put(cat.getDisplayName(), avgPercent);
            if (needsMatching(avgPercent)) {
                needsMatching = true;
            }
        }

        Map<String,Object> result = new HashMap<>();
        result.put("categoryScores", finalCategoryScores);
        result.put("needsMatching", needsMatching);

        if (needsMatching) {
            result.put("childId", dto.getChildId());
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