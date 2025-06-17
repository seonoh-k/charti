package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
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

    // 실제 사용할 Repository (주입)
    private final RecordSurveyRepository recordSurveyRepository;

    // 생성자: BaseService에도 전달되며, 필드에도 직접 저장
    public RecordSurveyService(RecordSurveyRepository repository, RecordSurveyRepository recordSurveyRepository) {
        super(repository);
        this.recordSurveyRepository = recordSurveyRepository;
    }

    /**
     * 특정 연령대의 기록 문진을 조회 (삭제되지 않은 것만)
     * - 연령대가 null이거나 ALL이면 전체 반환
     */
    public List<RecordSurvey> getByAgeGroup(AgeGroup ageGroup) {
        if (ageGroup != null && ageGroup != AgeGroup.ALL) {
            return recordSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup);
        }
        return recordSurveyRepository.findAllByDeletedFalse();
    }

    /**
     * 삭제되지 않은 전체 기록 문진 목록 반환
     */
    public List<RecordSurvey> getAllActiveSurveys() {
        return repository.findByDeletedFalse();
    }

    /**
     * 페이징 처리된 기록 문진 목록 조회 (정렬 기준/방향 및 연령대 필터 포함)
     */
    public Page<RecordSurvey> getPagedSurveys(AgeGroup ageGroup, int page, int size, String sortBy, String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(dir, sortBy));

        if (ageGroup != null && ageGroup != AgeGroup.ALL) {
            return recordSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup, pageRequest);
        } else {
            return recordSurveyRepository.findByDeletedFalse(pageRequest);
        }
    }

    /**
     * soft delete 처리 (deleted=true, deletedAt 설정)
     */
    public void softDelete(Long id) {
        RecordSurvey survey = get(id);
        survey.markAsDeleted();
        repository.save(survey);
    }
}
