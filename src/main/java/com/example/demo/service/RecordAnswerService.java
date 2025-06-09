package com.example.demo.service;

import com.example.demo.entity.Child;
import com.example.demo.entity.Member;
import com.example.demo.entity.RecordAnswer;
import com.example.demo.repository.RecordAnswerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordAnswerService extends BaseService<RecordAnswer, Long, RecordAnswerRepository> {

    public RecordAnswerService(RecordAnswerRepository repository) {
        super(repository);
    }

    public List<RecordAnswer> getAnswersByWriter(Member writer) {
        return getRepository().findByWriterAndDeletedFalse(writer);
    }

    public List<RecordAnswer> getAnswersByWriterAndChild(Member writer, Child child) {
        return getRepository().findByWriterAndChildAndDeletedFalse(writer, child);
    }

    public void softDelete(Long id) {
        RecordAnswer answer = get(id);
        answer.markAsDeleted();
        getRepository().save(answer);
    }
}
