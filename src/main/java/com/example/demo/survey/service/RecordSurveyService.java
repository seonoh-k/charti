package com.example.demo.survey.service;

import com.example.demo.service.BaseService;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.repository.RecordSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordSurveyService extends BaseService<RecordSurvey, RecordSurveyRepository> {


    public RecordSurveyService(RecordSurveyRepository repository) {
        super(repository);
    }

    // 삭제되지 않은 설문만 조회 [소프트딜리트[삭제처리되지않은]]
    public List<RecordSurvey> getAllActiveSurveys() {
        return repository.findByDeletedFalse();
    }


    public void softDelete(Long id) {
        RecordSurvey survey = get(id);
        survey.markAsDeleted();
        repository.save(survey);
    }
}
