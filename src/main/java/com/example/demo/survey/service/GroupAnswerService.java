package com.example.demo.survey.service;

import com.example.demo.survey.entity.GroupAnswer;
import com.example.demo.survey.repository.GroupAnswerRepository;
import com.example.demo.survey.entity.GroupSurvey;
import com.example.demo.survey.repository.GroupSurveyRepository;
import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.TargetGroup;
import com.example.demo.users.entity.Child;
import com.example.demo.users.service.ChildService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupAnswerService {
    private final GroupAnswerRepository answerRepo;
    private final ChildService childService;
    private final GroupSurveyRepository surveyRepo;

    // 생성
    @Transactional
    public void saveAnswers(Long childId,
                            AgeGroup ageGroup,
                            TargetGroup targetGroup,
                            List<Integer> answers) {
        Child child = childService.findById(childId);
        List<GroupSurvey> surveys = surveyRepo.findByAgeGroupAndDeletedFalse(ageGroup).stream()
                .filter(gs -> gs.getTargetGroup().filter(t->t == targetGroup).isPresent())
                .toList();
        if (surveys.size() != answers.size()) {
            throw new IllegalArgumentException("문진 항목 수와 응답 수 불일치");
        }
        for (int i = 0; i < surveys.size(); i++) {
            GroupSurvey s = surveys.get(i);
            int idx = answers.get(i);
            String text = switch(idx) {
                case 1 -> s.getAnswer1();
                case 2 -> s.getAnswer2();
                case 3 -> s.getAnswer3();
                case 4 -> s.getAnswer4();
                case 5 -> s.getAnswer5();
                default -> throw new IllegalArgumentException("1~5 사이 값 필요");
            };
            GroupAnswer a = new GroupAnswer();
            a.setSurvey(s);
            a.setChild(child);
            a.setAgeGroup(ageGroup);
            a.setTargetGroup(targetGroup);
            a.setCategory(s.getCategory());
            a.setQuestion(s.getQuestion());
            a.setAnswer(text);
            answerRepo.save(a);
        }
    }

    public List<GroupAnswer> findByChild(Long childId) {
        return answerRepo.findByChildIdAndDeletedFalseOrderByCreatedAtDesc(childId);
    }

    // 수정
    @Transactional
    public void updateAnswer(Long id, int newValue) {
        GroupAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변 없음: " + id));
        GroupSurvey s = surveyRepo.findById(a.getSurvey().getId())
                .orElseThrow(() -> new EntityNotFoundException("문진 항목 없음"));
        String newText = switch(newValue) {
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

    // API: 답변 수정
    @Transactional
    public void updateAnswerValue(Long id, int newValue) {
        GroupAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변없음 "+id));
        GroupSurvey s = surveyRepo.findById(a.getSurvey().getId())
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

    // 답변 삭제(soft delete)
    @Transactional
    public void deleteAnswer(Long id) {
        GroupAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변없음 "+id));
        a.markAsDeleted();
        answerRepo.save(a);
    }

}
