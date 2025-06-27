package com.example.demo.users.service;

import com.example.demo.dto.AdminDTO;
import com.example.demo.dto.request.AdminCreateRequest;
import com.example.demo.users.entity.Admin;
import com.example.demo.users.entity.Role;
import com.example.demo.users.exception.AdminAlreadyExistsException;
import com.example.demo.users.exception.AdminNotFoundException;
import com.example.demo.users.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;


    public AdminDTO getAdminByEmail(String email) throws AdminNotFoundException{
        Optional<AdminDTO> admin = adminRepository.getAdminDTOByUsername(email);
        if (admin.isEmpty()){
            throw new AdminNotFoundException();
        } else {
            AdminDTO adminDTO = admin.get();
            return adminDTO;
        }

    };
    public AdminDTO getAdminDTOByUUIDToAuth(String uuid) throws AdminNotFoundException{
        Optional<AdminDTO> admin = adminRepository.getAdminDTOByUUIDToAuth(uuid);
        if (admin.isEmpty()){
            throw new AdminNotFoundException();
        } else {
            AdminDTO adminDTO = admin.get();
            return adminDTO;
        }

    };

    public boolean existsAdminByUsername(String username){
        return adminRepository.existsByUsername(username);
    }
    public AdminCreateRequest createAdmin(AdminCreateRequest r) throws AdminAlreadyExistsException{

        String rawPassword = r.getPassword();
        String encode = passwordEncoder.encode(rawPassword);

        Admin admin = Admin.builder()
                .name(r.getName())
                .position(r.getPosition())
                .username(r.getUsername())
                .password(encode)
                .uuid(r.getUuid())
                .role(Role.ROLE_ADMIN)
                .phoneNumber(r.getPhoneNumber()).build();
        Boolean adminIsExist =  existsAdminByUsername(r.getUsername());

        if (adminIsExist){
            throw new AdminAlreadyExistsException();
        } else {
            Admin saved = adminRepository.save(admin);
            r.setId(saved.getId());
            return r;
        }

    }

}
