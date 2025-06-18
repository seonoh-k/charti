package com.example.demo.survey.service;

import com.example.demo.service.BaseService;
import com.example.demo.survey.dto.RecordSurveyAnswerDto;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.repository.RecordAnswerRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordAnswerService extends BaseService<RecordAnswer, RecordAnswerRepository> {

    private final RecordSurveyService recordSurveyService;
    private final RecordAnswerRepository recordAnswerRepository;

    /**
     * 생성자 주입을 통해 RecordSurveyService와 RecordAnswerRepository를 초기화합니다.
     * super(repository)는 BaseService에서 사용하는 repository를 초기화합니다.
     */
    public RecordAnswerService(RecordAnswerRepository repository,
                               RecordSurveyService recordSurveyService) {
        super(repository);
        this.recordSurveyService = recordSurveyService;
        this.recordAnswerRepository = repository;
    }

    /**
     * 기록 문진 답변을 저장합니다.
     * @param writer 작성자 (회원)
     * @param child 자녀 정보
     * @param dto 질문 ID와 답변 텍스트를 포함한 DTO
     */
    public void saveAnswer(Member writer, Child child, RecordSurveyAnswerDto dto) {
        RecordAnswer answer = new RecordAnswer();
        answer.setWriter(writer);
        answer.setChild(child);

        // 질문 ID로 실제 질문 엔티티 조회 후 설정
        RecordSurvey survey = recordSurveyService.findById(dto.getQuestionId());
        answer.setSurvey(survey);
        answer.setQuestion(survey.getQuestion()); // 중복 저장: 추후 질문 변경 시 이력 보존

        answer.setAnswer(dto.getText());

        repository.save(answer);
    }

    /**
     * 작성자 기준으로 삭제되지 않은 모든 기록 문진 답변을 조회합니다.
     */
    public List<RecordAnswer> getAnswersByWriter(Member writer) {
        return repository.findByWriterAndDeletedFalse(writer);
    }

    /**
     * 작성자 + 자녀 기준으로 삭제되지 않은 기록 문진 답변을 조회합니다.
     */
    public List<RecordAnswer> getAnswersByWriterAndChild(Member writer, Child child) {
        return repository.findByWriterAndChildAndDeletedFalse(writer, child);
    }

    /**
     * 특정 답변 ID를 기준으로 soft delete (삭제일자, 삭제 상태 설정) 처리합니다.
     */
    public void softDelete(Long id) {
        RecordAnswer answer = get(id); // BaseService의 get 메서드 활용
        answer.markAsDeleted();
        repository.save(answer);
    }

    /**
     * 인라인 수정 기능을 위한 답변 수정 메서드입니다.
     * @param answerId 수정할 답변의 ID
     * @param newAnswer 새로운 텍스트
     */
    public void updateAnswerText(Long answerId, String newAnswer) {
        RecordAnswer answer = recordAnswerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다."));
        answer.setAnswer(newAnswer);
        recordAnswerRepository.save(answer);
    }
}
