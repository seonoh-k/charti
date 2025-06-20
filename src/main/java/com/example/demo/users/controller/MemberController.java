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

    @GetMapping("/myInfo")
    public String myInfoPage(Model model) {
        UserDTO userinfo = authService.getLoginUser();
        Role role = Role.valueOf(userinfo.getRole());

        AddressDTO address = switch (role) {
            case ROLE_MEMBER -> addressService.getByMemberUid(userinfo.getUuid());
            case ROLE_EXPERT -> addressService.getByExpertUid(userinfo.getUuid());
            case ROLE_MANAGER -> addressService.getGroupIdByManagerUid(userinfo.getUuid());
            default -> throw new RuntimeException("주소를 불러올 수 없습니다.");
        };

        userinfo.setAddress(address);
        model.addAttribute("userInfo", userinfo);
        return "/myInfo"; // templates/user/my-info.html
    }


}
