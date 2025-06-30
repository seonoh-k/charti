package com.example.demo.survey.service;

import com.example.demo.survey.dto.DailyAnswerDto;
import com.example.demo.survey.entity.DailyAnswer;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.repository.DailyAnswerRepository;
import com.example.demo.users.entity.Child;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

    public List<DailyAnswerDto> getPagedAnswerList(Long childId) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<DailyAnswer> ansList = repo.findAllByChildIdAndDeletedFalse(childId, pageable);

        List<DailyAnswerDto> dtoList = new ArrayList<>();
        for(DailyAnswer d : ansList.getContent()) {
            dtoList.add(new DailyAnswerDto(d));
        }

        return dtoList;
    }

    // 특정 자녀가 오늘 데일리 문진을 제출했는지 여부를 확인
    @Transactional(readOnly = true)
    public boolean hasAnsweredToday(Child child) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        return repo.existsByChildAndCreatedAtBetween(child, start, end);
    }
    public DailyAnswer save(DailyAnswer da) {
        return repo.save(da);
    }
}

