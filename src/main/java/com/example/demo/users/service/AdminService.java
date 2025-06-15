package com.example.demo.users.service;

import com.example.demo.users.repository.AdminRepository;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;


}
