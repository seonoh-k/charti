package com.example.demo.users.service;

import com.example.demo.dto.AdminDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.AdminCreateRequest;
import com.example.demo.users.entity.Admin;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.exception.AdminAlreadyExistsException;
import com.example.demo.users.exception.AdminNotFoundException;
import com.example.demo.users.repository.AdminQueryRepository;
import com.example.demo.users.repository.AdminRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final AdminQueryRepository adminQueryRepository;
    private final PasswordEncoder passwordEncoder;

    public String getAdminUUIDById(Long id) throws AdminNotFoundException{
        Optional<String> byId = adminRepository.getAdminUUIDById(id);
        if (byId.isEmpty()){
            throw new AdminNotFoundException();
        } else {
            return byId.get();
        }
    }
    public AdminDTO getAdminByEmail(String email) throws AdminNotFoundException{
        Optional<AdminDTO> admin = adminRepository.getAdminDTOByUsername(email);
        if (admin.isEmpty()){
            throw new AdminNotFoundException();
        } else {
            AdminDTO adminDTO = admin.get();
            return adminDTO;
        }

    };
    public AdminDTO getAdminDTOById(Long id){
        Optional<AdminDTO> admin = adminQueryRepository.getAdminDTOById(id);
        if (admin.isEmpty()){
            throw new AdminNotFoundException();
        } else {
            AdminDTO adminDTO = admin.get();
            return adminDTO;
        }
    }
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
    public PagingResultDTO<AdminDTO, Admin> searchAdminList(String type, String keyword, Pageable pageable) {

        Page<AdminDTO> result = adminQueryRepository.searchAdminList(type, keyword, pageable);

        return new PagingResultDTO<>(result);

    }

    public PagingResultDTO<AdminDTO, Admin> getAdminList(Pageable pageable){
        Page<AdminDTO> result = adminQueryRepository.getAdminList(pageable);

        return new PagingResultDTO<>(result);
    }

    @Transactional
    public boolean initializePassword(Long id){
        boolean existsById = adminRepository.existsById(id);
        final String initiPassword = "@Charti1234";
        String encodePassword = passwordEncoder.encode(initiPassword);
        if(existsById){
            adminRepository.initializePassword(id,encodePassword);
            return true;
        } else {
            return existsById;
        }

    }

    public Admin getAdminById(Long id){
        return adminRepository.findById(id).get();
    }

}
