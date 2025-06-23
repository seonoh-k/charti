package com.example.demo.users.controller;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.Role;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.AuthService;
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
    private final AuthService authService;
    private final AddressService addressService;

    @GetMapping("/member")
    public String showMemberPage() {
        log.info("[GET] 👤 request Member Page");
        return "member";
    }

    @GetMapping("/myInfo")
    public String myInfoPage() {
        UserDTO userDTO = authService.getLoginUser();
        Role role = Role.valueOf(userDTO.getRole());

        // ✅ 권한에 따라 마이페이지 리다이렉트
        return switch (role) {
            case ROLE_MEMBER -> "redirect:/member/myPage";
            case ROLE_EXPERT -> "redirect:/expert/myPage";
            case ROLE_MANAGER -> "redirect:/manager/myPage";
            default -> "redirect:/error";
        };
    }

    @GetMapping("/member/myPage")
    public String showMemberMyPage(Model model) {
        log.info("[GET] 👨‍💼 request member Page");
        UserDTO userDTO = authService.getLoginUser();

        AddressDTO address =  addressService.getByMemberUid(userDTO.getUuid());

        userDTO.setAddress(address);
        model.addAttribute("userInfo", userDTO);

        return "member/myPage";
    }

    @GetMapping("/main")
    public String showMainPage(Model model){

        log.info("[GET] 🟢 AuthController.showMainPage");

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String uuid = (String) authentication.getPrincipal();
//        log.info(uuid);

//        String role = authentication.getAuthorities()
//                .stream()
//                .findFirst()
//                .get()
//                .toString();

        try{

            UserDTO userDTO = authService.getLoginUser();
            model.addAttribute("email", userDTO.getName());
            model.addAttribute("role",userDTO.getRole());

        } catch (UserNotFoundException e){
            log.info(" ⚠ AuthController.showMainPage MemberNotFoundException ");
        }
        return "main";

    }


}
