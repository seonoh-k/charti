package com.example.demo.users.service;

import com.example.demo.service.BaseService;
import com.example.demo.users.entity.RiskCategory;
import com.example.demo.users.repository.RiskCategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class RiskCategoryService extends BaseService<RiskCategory, RiskCategoryRepository> {
    public RiskCategoryService(RiskCategoryRepository repository) {
        super(repository);
    }

    public RiskCategory createRiskCategory(RiskCategory riskCategory) {
        return repository.save(riskCategory);
    }
}
