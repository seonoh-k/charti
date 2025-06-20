package com.example.demo.survey.service;

import com.example.demo.survey.entity.DailyAnswer;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.repository.DailyAnswerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyAnswerService {
    private final DailyAnswerRepository repo;

    public List<DailyAnswer> getAnswersByChild(Long childId) {
        return repo.findByChildIdAndDeletedFalseOrderByCreatedAtDesc(childId);
    }

    public void updateAnswerValue(Long id, int value) {
        DailyAnswer da = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변이 없습니다. id=" + id));

        DailySurvey s = da.getSurvey();
        String newText;
        switch (value) {
            case 1: newText = s.getAnswer1(); break;
            case 2: newText = s.getAnswer2(); break;
            case 3: newText = s.getAnswer3(); break;
            case 4: newText = s.getAnswer4(); break;
            case 5: newText = s.getAnswer5(); break;
            default: throw new IllegalArgumentException("유효한 값(1~5)이 아닙니다: " + value);
        }

        da.setAnswer(newText);
        repo.save(da);
    }

    public void deleteAnswer(Long id) {
        DailyAnswer da = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다."));
        da.markAsDeleted();  // BaseEntity 의 soft-delete 메서드
        repo.save(da);
    }
}

