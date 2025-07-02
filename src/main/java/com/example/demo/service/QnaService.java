package com.example.demo.service;

import com.example.demo.entity.Qna;
import com.example.demo.enums.QnaCategory;
import com.example.demo.repository.QnaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class QnaService extends BaseService<Qna, QnaRepository> {

    public QnaService(QnaRepository repository) {
        super(repository);
    }

    public Page<Qna> getPagedList(Long id, QnaCategory category, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, "createdAt");

        if(id != null && category == null) {
            return repository.findAllByUsers_Id(id, pageable);
        }else if(id != null) {
            return repository.findAllByUsers_IdAndCategory(id, category, pageable);
        }else if(category != null) {
            return repository.findByCategoryAndDeletedFalse(category, pageable);
        }else {
            return repository.findAllByDeletedFalse(pageable);
        }
    }

    public Page<Qna> getAdminPagedList(
            QnaCategory category,
            String status,
            String keyword,
            int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, "createdAt");

        Specification<Qna> spec = Specification.where(null);

        if (category != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("category"), category));
        }

        if (status != null) {
            if (status.equals("waiting")) {
                spec = spec.and((r, q, cb) -> cb.isFalse(r.get("isAnswered")));
            } else if (status.equals("done")) {
                spec = spec.and((r, q, cb) -> cb.isTrue(r.get("isAnswered")));
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((r, q, cb) ->
                    cb.like(cb.lower(r.get("title")), "%" + keyword.toLowerCase() + "%")
            );
        }

        return repository.findAll(spec, pageable);
    }

    public Long createQna(Qna qna) {
        Qna saved = repository.save(qna);
        return saved.getId();
    }
}
