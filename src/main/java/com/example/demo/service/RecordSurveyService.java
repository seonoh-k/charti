package com.example.demo.service;

import com.example.demo.entity.RecordSurvey;
import com.example.demo.repository.RecordSurveyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordSurveyService extends BaseService<RecordSurvey, Long, RecordSurveyRepository> {

    public RecordSurveyService(RecordSurveyRepository repository) {
        super(repository);
    }

    // 삭제되지 않은 설문만 조회 [소프트딜리트[삭제처리되지않은]]
    public List<RecordSurvey> getAllActiveSurveys() {
        return getRepository().findByDeletedFalse();
    }


    public void softDelete(Long id) {
        RecordSurvey survey = get(id);
        survey.markAsDeleted();
        getRepository().save(survey);
    }
}
