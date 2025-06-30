package com.example.demo.users.controller;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.ChildDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Role;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.demo.users.service.UserService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final UserService userService;
    private final AuthService authService;
    private final AddressService addressService;
    private final ChildService childService;

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
        List<Child> children = childService.findByUsersId(userDTO.getId());
        AddressDTO address =  addressService.getAddressByUid(userDTO.getUuid());

        userDTO.setAddress(address);
        model.addAttribute("userInfo", userDTO);
        model.addAttribute("children", children);
        return "member/myPage";
    }
    @GetMapping("/member/child")
    public String showMemberChild(Model model) {
        log.info("[GET] 👨‍💼 request member Child");
        UserDTO userDTO = authService.getLoginUser();

        List<Child> children = childService.findByUsersId(userDTO.getId());

        model.addAttribute("userInfo", userDTO);
        model.addAttribute("children", children);
        return "member/child";
    }


    @PostMapping("/member/update")
    public String updateMemberInfo(
            @ModelAttribute UserUpdateRequest req,
            Authentication authentication,
            RedirectAttributes rttr) {

        // 1. 인증된 유저 정보 확인
        String uid = authentication.getPrincipal().toString();

        // 2. 유효성 검사 (빈 값 체크 등)
        if (req.getName() == null || req.getName().isBlank() ||
                req.getNickname() == null || req.getNickname().isBlank() ||
                req.getPhoneNumber() == null || req.getPhoneNumber().isBlank() ||
                req.getAddressId() == null) {
            rttr.addFlashAttribute("msg", "입력값을 모두 입력해주세요.");
            return "redirect:/member/myPage";  // 다시 마이페이지로
        }

        // 3. 서비스 호출 (실제 정보 수정)
        try {
            userService.updateMember(req, uid);
            rttr.addFlashAttribute("msg", "정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("msg", "수정 중 오류 발생: " + e.getMessage());
        }

        // 4. 리다이렉트(페이지 새로고침)
        return "redirect:/member/myPage";
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
