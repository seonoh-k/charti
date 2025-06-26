package com.example.demo.service;

import com.example.demo.entity.QnaAnswer;
import com.example.demo.repository.QnaAnswerRepository;
import org.springframework.stereotype.Service;

@Service
public class QnaAnswerService extends BaseService<QnaAnswer, QnaAnswerRepository> {
    public QnaAnswerService(QnaAnswerRepository repository) {
        super(repository);
    }

    public QnaAnswer createAnswer(QnaAnswer qnaAnswer) {
        return repository.save(qnaAnswer);
    }
}
