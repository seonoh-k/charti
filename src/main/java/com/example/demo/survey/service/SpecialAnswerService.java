// com.example.demo.survey.service.SpecialAnswerService.java
package com.example.demo.survey.service;

import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.entity.SpecialSurvey;
import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.repository.SpecialAnswerRepository;
import com.example.demo.survey.repository.SpecialSurveyRepository;
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
    private final SpecialSurveyRepository surveyRepo;
    private final SpecialSurveyRepository surveyRepository;
    private final ChildRepository childRepository;

    @Transactional
    public void saveAnswers(Long childId,
                            AgeGroup ageGroup,
                            SurveyCategory category,
                            List<Integer> answers) {
        Child child = childService.findById(childId);
        List<SpecialSurvey> surveys = surveyRepo
                .findByAgeGroupAndDeletedFalse(ageGroup)
                .stream()
                .filter(s -> s.getCategory() == category)
                .toList();

        if (surveys.size() != answers.size()) {
            throw new IllegalArgumentException("문진 항목 수와 응답 수 불일치");
        }

        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey s = surveys.get(i);
            int idx = answers.get(i);
            String text = switch(idx) {
                case 1 -> s.getAnswer1();
                case 2 -> s.getAnswer2();
                case 3 -> s.getAnswer3();
                case 4 -> s.getAnswer4();
                case 5 -> s.getAnswer5();
                default -> throw new IllegalArgumentException("1~5 사이 값 필요");
            };
            SpecialAnswer a = new SpecialAnswer();
            a.setSurvey(s);
            a.setChild(child);
            a.setAgeGroup(ageGroup);
            a.setCategory(s.getCategory());
            a.setQuestion(s.getQuestion());
            a.setAnswer(text);
            answerRepo.save(a);
        }
    }

    // [신규 메소드 추가] - 저장된 SpecialAnswer 엔티티 목록을 반환
    @Transactional
    public List<SpecialAnswer> saveAndGetAnswers(Long childId, AgeGroup ageGroup, SurveyCategory category, List<Integer> answerValues) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("Child not found: " + childId));

        List<SpecialSurvey> surveys = surveyRepo.findByAgeGroupAndCategoryAndDeletedFalse(ageGroup, category);

        if (surveys.size() != answerValues.size()) {
            throw new IllegalArgumentException("답변 수가 문항 수와 일치하지 않습니다.");
        }

        Map<Long, SpecialSurvey> surveyMap = surveys.stream()
                .collect(Collectors.toMap(SpecialSurvey::getId, Function.identity()));

        List<SpecialAnswer> savedAnswers = new ArrayList<>();

        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey survey = surveys.get(i);
            int answerValue = answerValues.get(i);
            String answerText = getAnswerTextByValue(survey, answerValue);

            SpecialAnswer specialAnswer = new SpecialAnswer();
            specialAnswer.setChild(child);
            specialAnswer.setSurvey(survey);
            specialAnswer.setAgeGroup(ageGroup);
            specialAnswer.setCategory(category);
            specialAnswer.setQuestion(survey.getQuestion());
            specialAnswer.setAnswer(answerText);

            savedAnswers.add(answerRepo.save(specialAnswer));
        }
        return savedAnswers;
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
        SpecialSurvey s = surveyRepo.findById(a.getSurvey().getId())
                .orElseThrow(() -> new EntityNotFoundException("설문없음 "+a.getSurvey().getId()));
        String newText = switch (newValue) {
            case 1 -> s.getAnswer1();
            case 2 -> s.getAnswer2();
            case 3 -> s.getAnswer3();
            case 4 -> s.getAnswer4();
            case 5 -> s.getAnswer5();
            default -> throw new IllegalArgumentException("1~5 사이 값 필요");
        };
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