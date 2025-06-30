// com.example.demo.survey.service.SpecialAnswerService.java
package com.example.demo.survey.service;

import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.dto.SpecialSurveyRequestDto;
import com.example.demo.survey.dto.SurveySetSubmitRequestDto;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.entity.SpecialSurvey;
import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.repository.SpecialAnswerRepository;
import com.example.demo.survey.repository.SpecialSurveyRepository;
import com.example.demo.survey.repository.SurveySetRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.users.service.ChildService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialAnswerService {
    private final SpecialAnswerRepository answerRepo;
    private final ChildService childService;
    private final SurveySetRepository surveySetRepo;
    private final SpecialSurveyRepository surveyRepo;
    private final SpecialSurveyRepository surveyRepository;
    private final ChildRepository childRepo;

    @Transactional
    public void saveAnswers(Long childId,
                            Long setId,
                            List<SurveySetSubmitRequestDto.AnswerDto> answers) {
        Child child = childService.findById(childId);
        SurveySet set = surveySetRepo.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세트: " + setId));
        List<SpecialSurvey> surveys = set.getSpecialSurveys();

        if (surveys.size() != answers.size()) {
            throw new IllegalArgumentException("문진 항목 수와 응답 수 불일치");
        }

        Map<Long, Integer> answerMap = answers.stream()
                .collect(Collectors.toMap(SurveySetSubmitRequestDto.AnswerDto::getSurveyId,
                        SurveySetSubmitRequestDto.AnswerDto::getAnswerValue));

        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey s = surveys.get(i);
            int idx = answerMap.get(s.getId());
            String text = switch(idx) {
                case 1 -> s.getAnswer1();
                case 2 -> s.getAnswer2();
                case 3 -> s.getAnswer3();
                case 4 -> s.getAnswer4();
                case 5 -> s.getAnswer5();
                default -> throw new IllegalArgumentException("1~5 사이 값 필요");
            };
            SpecialAnswer a = new SpecialAnswer();
            a.setSurveySet(set);
            a.setChild(child);
            a.setAgeGroup(s.getAgeGroup());
            a.setCategory(s.getCategory());
            a.setQuestion(s.getQuestion());
            a.setAnswer(text);
            answerRepo.save(a);
        }
    }

    // [신규 메소드 추가] - 저장된 SpecialAnswer 엔티티 목록을 반환
    @Transactional
    public List<SpecialAnswer> saveAndGetAnswers(
            Long childId,
            AgeGroup ageGroup,
            SurveyCategory category,
            List<SpecialSurveyRequestDto.AnswerDto> answers
    ) {
        // 1) Child 조회
        Child child = childRepo.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("자녀를 찾을 수 없습니다: " + childId));

        List<SpecialAnswer> saved = new ArrayList<>();
        for (SpecialSurveyRequestDto.AnswerDto dto : answers) {

            // 3) SpecialAnswer 엔티티 채우기
            SpecialAnswer ans = new SpecialAnswer();
            ans.setChild(child);
            ans.setAgeGroup(ageGroup);
            ans.setCategory(category);
            ans.setQuestion(dto.getQuestion());
            ans.setAnswer(dto.getAnswerText());

            // 2) SurveySet 조회 (FK)
            if(dto.getSurveySetId() != null) {
                SurveySet set = surveySetRepo.findById(dto.getSurveySetId())
                        .orElseThrow(() -> new EntityNotFoundException("SurveySet을 찾을 수 없습니다: " + dto.getSurveySetId()));
                ans.setSurveySet(set);
            }

            // 4) 저장
            saved.add(answerRepo.save(ans));
        }
        return saved;
    }

    @Transactional
    public List<SpecialAnswer> saveAndGetAnswers2(
            Long childId,
            AgeGroup ageGroup,
            SurveyCategory category,
            List<SurveySetSubmitRequestDto.AnswerDto> answers
    ) {
        // 1) Child 조회
        Child child = childRepo.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("자녀를 찾을 수 없습니다: " + childId));

        Map<Long, Integer> answerMap = answers.stream()
                .collect(Collectors.toMap(SurveySetSubmitRequestDto.AnswerDto::getSurveyId,
                        SurveySetSubmitRequestDto.AnswerDto::getAnswerValue));

        List<SpecialAnswer> saved = new ArrayList<>();
        for (SurveySetSubmitRequestDto.AnswerDto dto : answers) {
            SpecialSurvey s = surveyRepo.findById(dto.getSurveyId()).get();

            int idx = answerMap.get(s.getId());
            String text = switch (idx) {
                case 1 -> s.getAnswer1();
                case 2 -> s.getAnswer2();
                case 3 -> s.getAnswer3();
                case 4 -> s.getAnswer4();
                case 5 -> s.getAnswer5();
                default -> throw new IllegalArgumentException("1~5 사이 값 필요");
            };

            // 2) SurveySet 조회 (FK)
            SurveySet set = surveySetRepo.findById(dto.getSurveyId())
                    .orElseThrow(() -> new EntityNotFoundException("SurveySet을 찾을 수 없습니다: " + dto.getSurveyId()));

            // 3) SpecialAnswer 엔티티 채우기
            SpecialAnswer ans = new SpecialAnswer();
            ans.setChild(child);
            ans.setAgeGroup(ageGroup);
            ans.setCategory(category);
            ans.setQuestion(dto.getQuestion());
            ans.setAnswer(text);

            // 4) 저장
            saved.add(answerRepo.save(ans));
        }
        return saved;
    }

    // 답변 값(1~5)에 해당하는 답변 텍스트를 가져오는 헬퍼 메소드
    private String getAnswerTextByValue(SpecialSurvey survey, int value) {
        return switch (value) {
            case 1 -> survey.getAnswer1();
            case 2 -> survey.getAnswer2();
            case 3 -> survey.getAnswer3();
            case 4 -> survey.getAnswer4();
            case 5 -> survey.getAnswer5();
            default -> throw new IllegalArgumentException("Invalid answer value: " + value);
        };
    }


    public List<SpecialAnswer> findByChild(Long childId) {
        return answerRepo.findByChildIdAndDeletedFalseOrderByCreatedAtDesc(childId);
    }

    @Transactional
    public void updateAnswerValue(Long id, int newValue) {
        SpecialAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변없음 "+id));

        // 1) 이 답변이 속한 SurveySet 로딩
        SurveySet set = surveySetRepo.findById(a.getSurveySet().getSetId())
                .orElseThrow(() -> new EntityNotFoundException("세트 없음: " + a.getSurveySet().getSetId()));

        // 2) 기존 answer 엔티티에 저장된 question 텍스트로, 해당 문진 항목 객체를 찾음
        SpecialSurvey s = set.getSpecialSurveys().stream()
                .filter(ss -> ss.getQuestion().equals(a.getQuestion()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("문진 항목 없음: " + a.getQuestion())
                );

        // 3) 선택된 숫자(newValue)에 따라 새 텍스트 매핑
        String newText = switch (newValue) {
            case 1 -> s.getAnswer1();
            case 2 -> s.getAnswer2();
            case 3 -> s.getAnswer3();
            case 4 -> s.getAnswer4();
            case 5 -> s.getAnswer5();
            default -> throw new IllegalArgumentException("1~5 사이 값 필요");
        };

        // 4) 엔티티에 반영 후 저장
        a.setAnswer(newText);
        answerRepo.save(a);
    }

    @Transactional
    public void deleteAnswer(Long id) {
        SpecialAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변없음 "+id));
        a.markAsDeleted();
        answerRepo.save(a);
    }
}