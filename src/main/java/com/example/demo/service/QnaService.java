package com.example.demo.service;

import com.example.demo.dto.QnaDTO;
import com.example.demo.entity.Qna;
import com.example.demo.enums.QnaCategory;
import com.example.demo.repository.QnaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QnaService extends BaseService<Qna, QnaRepository> {

    public QnaService(QnaRepository repository) {
        super(repository);
    }

    public List<QnaDTO> getPagedList(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.Direction.DESC, "createdAt");
        Page<Qna> qnaPage = repository.findALLQna(pageable);

        List<QnaDTO> qnaDTOList = new ArrayList<>();
        for(Qna qna : qnaPage) {
            qnaDTOList.add(new QnaDTO(qna));
        }

        return qnaDTOList;
    }

    public List<QnaDTO> getPagedCategoryList(QnaCategory category, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.Direction.DESC, "createdAt");
        Page<Qna> qnaPage = repository.findQnaByCategory(category, pageable);
        List<QnaDTO> qnaDTOList = new ArrayList<>();

        for(Qna qna : qnaPage) {
            qnaDTOList.add(new QnaDTO(qna));
        }

        return qnaDTOList;
    }
}
