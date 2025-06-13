package com.example.demo.users.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.users.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.demo.users.service.UserService;
@Controller
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final UserService userService;

    @GetMapping("/member")
    public String showMemberPage() {
        log.info("[GET] 👤 request Member Page");
        return "member";
    }

    @GetMapping("/main")
    public String showMainPage(Model model){
        log.info("[GET] 🟢 AuthController.showMainPage");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .get()
                .toString();
        try{
            UserDTO userDTO = userService.getMemberByUUID(uuid);
            model.addAttribute("email", userDTO.getName());
            model.addAttribute("role",role);
        } catch (UserNotFoundException e){
            log.info(" ⚠ AuthController.showMainPage MemberNotFoundException ");
        }
        return "main";
    }







}
