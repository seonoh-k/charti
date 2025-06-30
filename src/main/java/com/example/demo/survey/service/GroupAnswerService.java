package com.example.demo.survey.service;

import com.example.demo.survey.dto.SurveySetSubmitRequestDto;
import com.example.demo.survey.entity.GroupAnswer;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.repository.GroupAnswerRepository;
import com.example.demo.survey.entity.GroupSurvey;
import com.example.demo.survey.repository.GroupSurveyRepository;
import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.repository.SurveySetRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.service.ChildService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupAnswerService {
    private final GroupAnswerRepository answerRepo;
    private final ChildService childService;
    private final SurveySetRepository surveySetRepo;

    // 생성
    @Transactional
    public void saveAnswers(Long childId,
                            Long setId,
                            List<SurveySetSubmitRequestDto.AnswerDto> answerList) {
                Child child = childService.findById(childId);
                        SurveySet set = surveySetRepo.findById(setId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세트: " + setId));
                List<GroupSurvey> surveys = set.getGroupSurveys();
        if (surveys.size() != answerList.size()) {
            throw new IllegalArgumentException("문진 항목 수와 응답 수 불일치");
        }

        Map<Long, Integer> answerMap = answerList.stream()
                .collect(Collectors.toMap(SurveySetSubmitRequestDto.AnswerDto::getSurveyId,
                        SurveySetSubmitRequestDto.AnswerDto::getAnswerValue));

        for (int i = 0; i < surveys.size(); i++) {
            GroupSurvey s = surveys.get(i);
            int idx = answerMap.get(s.getId());
            String text = switch(idx) {
                case 1 -> s.getAnswer1();
                case 2 -> s.getAnswer2();
                case 3 -> s.getAnswer3();
                case 4 -> s.getAnswer4();
                case 5 -> s.getAnswer5();
                default -> throw new IllegalArgumentException("1~5 사이 값 필요");
            };
            GroupAnswer a = new GroupAnswer();
            a.setSurveySet(set);
            a.setChild(child);
            a.setAgeGroup(s.getAgeGroup());
            a.setTargetGroup(s.getTargetGroup().orElseThrow());
            a.setCategory(s.getCategory());
            a.setQuestion(s.getQuestion());
            a.setAnswer(text);
            answerRepo.save(a);
        }
    }

    public List<GroupAnswer> findByChild(Long childId) {
        return answerRepo.findByChildIdWithDetails(childId);
    }


    // 수정
    @Transactional
    public void updateAnswer(Long id, int newValue) {
        GroupAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변 없음: " + id));

        // 1) 이 답변이 속한 SurveySet 로딩
        SurveySet set = surveySetRepo.findById(a.getSurveySet().getSetId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "세트 없음: " + a.getSurveySet().getSetId()));

        // 2) question 텍스트로 원본 GroupSurvey 객체 찾기
        GroupSurvey s = set.getGroupSurveys().stream()
                .filter(gs -> gs.getQuestion().equals(a.getQuestion()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "문진 항목 없음: " + a.getQuestion()));

        // 3) newValue에 맞는 answer 텍스트 선택
        String newText = switch (newValue) {
            case 1 -> s.getAnswer1();
            case 2 -> s.getAnswer2();
            case 3 -> s.getAnswer3();
            case 4 -> s.getAnswer4();
            case 5 -> s.getAnswer5();
            default -> throw new IllegalArgumentException("1~5 사이 값 필요");
        };

        // 4) 저장
        a.setAnswer(newText);
        answerRepo.save(a);
    }

    // API: 답변 수정
    @Transactional
    public void updateAnswerValue(Long id, int newValue) {
        GroupAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변없음 "+id));

        // 1) 이 답변이 속한 SurveySet 로딩
        SurveySet set = surveySetRepo.findById(a.getSurveySet().getSetId())
                .orElseThrow(() -> new EntityNotFoundException("세트 없음: " + a.getSurveySet().getSetId()));

        // 2) 기존 answer 엔티티에 저장된 question 텍스트로, 해당 문진 항목 객체를 찾음
        GroupSurvey s = set.getGroupSurveys().stream()
                .filter(gs -> gs.getQuestion().equals(a.getQuestion()))
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
        a.setAnswer(newText);
        answerRepo.save(a);
    }

    // 답변 삭제(soft delete)
    @Transactional
    public void deleteAnswer(Long id) {
        GroupAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변없음 "+id));
        a.markAsDeleted();
        answerRepo.save(a);
    }

}
