package com.example.demo.service;


import com.example.demo.dto.UserDTO;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.repository.ManagerRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Test
    void 담당자로_기본회원정보_불러오기() {
        Optional<Manager> byId = managerRepository.findById(38L);
        if (byId.isPresent()){
            String password = byId.get().getUsers().getPassword();
            assertNotNull(byId.get());
            assertEquals("qwer1234",password);
            assertEquals("Manager One",byId.get().getUsers().getName());
            assertEquals(byId.get().getUsers().getRole(),Role.ROLE_MANAGER);
        }

//        // then
//        assertNotNull(result);
//        assertEquals("manager1", result.getUsername());
//        assertEquals("홍길동", result.getName());
//        assertEquals(Role.ROLE_MANAGER, result.getRole());
    }

    @Test
    void 담당자_신청쿼리(){

    }
}
