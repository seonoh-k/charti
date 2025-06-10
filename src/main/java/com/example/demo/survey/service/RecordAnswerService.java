package com.example.demo.survey.service;

import com.example.demo.service.BaseService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.survey.repository.RecordAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordAnswerService extends BaseService<RecordAnswer, RecordAnswerRepository> {

    public RecordAnswerService(RecordAnswerRepository repository) {
        super(repository);
    }

    public List<RecordAnswer> getAnswersByWriter(Member writer) {
        return repository.findByWriterAndDeletedFalse(writer);
    }

    public List<RecordAnswer> getAnswersByWriterAndChild(Member writer, Child child) {
        return repository.findByWriterAndChildAndDeletedFalse(writer, child);
    }

    public void softDelete(Long id) {
        RecordAnswer answer = get(id);
        answer.markAsDeleted();
        repository.save(answer);
    }
}
