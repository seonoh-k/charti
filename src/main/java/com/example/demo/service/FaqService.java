package com.example.demo.service;

import com.example.demo.entity.FAQ;
import com.example.demo.repository.FaqRepository;
import org.springframework.stereotype.Service;

@Service
public class FaqService extends BaseService<FAQ, FaqRepository> {

    public FaqService(FaqRepository repository) {
        super(repository);
    }
}
