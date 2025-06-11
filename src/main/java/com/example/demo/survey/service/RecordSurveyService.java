package com.example.demo.survey.service;

import com.example.demo.service.BaseService;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.repository.RecordSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordSurveyService extends BaseService<RecordSurvey, RecordSurveyRepository> {

    private final RecordSurveyRepository recordSurveyRepository;

    public RecordSurveyService(RecordSurveyRepository repository, RecordSurveyRepository recordSurveyRepository) {
        super(repository);
        this.recordSurveyRepository = recordSurveyRepository;
    }


    public List<RecordSurvey> getByAgeGroup(String ageGroup) {
        if (ageGroup != null && !ageGroup.isBlank()) {
            return recordSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup);
        }
        return recordSurveyRepository.findAllByDeletedFalse();
    }


    public List<RecordSurvey> getAllActiveSurveys() {
        return repository.findByDeletedFalse();
    }

    public Page<RecordSurvey> getPagedSurveys(String ageGroup, int page, int size, String sortBy, String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(dir, sortBy));

        if (ageGroup != null && !ageGroup.isBlank()) {
            return recordSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup, pageRequest);
        } else {
            return recordSurveyRepository.findByDeletedFalse(pageRequest);
        }
    }



    public void softDelete(Long id) {
        RecordSurvey survey = get(id);
        survey.markAsDeleted();
        repository.save(survey);
    }
}
