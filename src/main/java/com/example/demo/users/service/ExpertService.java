package com.example.demo.users.service;

import com.example.demo.users.repository.ExpertRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ExpertService {

    private final ExpertRepository expertRepository;



}
