package com.example.demo.service;

import com.example.demo.entity.Qna;
import com.example.demo.enums.QnaCategory;
import com.example.demo.repository.QnaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class QnaService extends BaseService<Qna, QnaRepository> {

    public QnaService(QnaRepository repository) {
        super(repository);
    }

    public Page<Qna> getPagedList(QnaCategory category, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, "createdAt");

        if(category == null) {
            return repository.findAllByDeletedFalse(pageable);
        }
        return repository.findByCategoryAndDeletedFalse(category, pageable);
    }

    public Page<Qna> getAdminPagedList(QnaCategory category, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, "createdAt");

        if(category == null) {
            return repository.findAll(pageable);
        }
        return repository.findAllByCategory(category, pageable);
    }

    public Long createQna(Qna qna) {
        Qna saved = repository.save(qna);
        return saved.getId();
    }
}
