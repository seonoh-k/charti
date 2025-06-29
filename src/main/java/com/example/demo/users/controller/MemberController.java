package com.example.demo.users.controller;

import com.example.demo.dto.*;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.service.AddressService;
import com.example.demo.survey.dto.DailyAnswerDto;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.survey.service.DailyAnswerService;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Role;
import com.example.demo.users.exception.UserNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final UserService userService;
    private final ChildService childService;
    private final AuthService authService;
    private final AddressService addressService;
    private final DailyAnswerService dailyAnswerService;
    private final RecordAnswerService recordAnswerService;
    private final MemberService memberService;

    @GetMapping("/member")
    public String showMemberPage() {
        log.info("[GET] 👤 request Member Page");
        return "member";
    }

    @GetMapping("/mypage")
    public String myInfoPage() {
        UserDTO userDTO = authService.getLoginUser();
        Role role = Role.valueOf(userDTO.getRole());

        // ✅ 권한에 따라 마이페이지 리다이렉트
        return switch (role) {
            case ROLE_MEMBER -> "redirect:/member/main";
            case ROLE_EXPERT -> "redirect:/expert/myPage";
            case ROLE_MANAGER -> "redirect:/manager/myPage";
            default -> "redirect:/error";
        };
    }

    @GetMapping("/member/main")
    public String showMyPageMain(Model model) {
        UserDTO user = authService.getLoginUser();

        if(!user.getRole().equals(Role.ROLE_MEMBER.getKey())) {
            return "redirect:/error";
        }
        Member memberInfo = memberService.get(user.getId());
        MemberDTO member = new MemberDTO(memberInfo);

        List<Child> children = memberInfo.getChildren();
        List<ChildDTO> childList = new ArrayList<>();
        for(Child child : children) {
            // 데일리 문진 이력 조회 - 최근 5개
            List<DailyAnswerDto> dailyAnswer = dailyAnswerService.getPagedAnswerList(child.getId());
            List<DailyAnswerDto> dAnswer = new ArrayList<>(
                    dailyAnswer.stream().collect(Collectors.toMap(
                            DailyAnswerDto::getCreated,
                            DailyAnswerDto -> DailyAnswerDto,
                            (existing, replacement) -> existing
                    )).values()
            );
            // 기록 문진 이력 조회 - 최근 5개
            List<RecordAnswerResponse> recordAnswer = recordAnswerService.getPagedAnswerList(child.getId());
            List<RecordAnswerResponse> rAnswer = new ArrayList<>(
                    recordAnswer.stream().collect(Collectors.toMap(
                            RecordAnswerResponse::getCreated,
                            RecordAnswerResponse -> RecordAnswerResponse,
                            (existing, replacement) -> existing
                    )).values()
            );
            childList.add(new ChildDTO(child, dAnswer, rAnswer));
        }

        model.addAttribute("member", member);
        model.addAttribute("childList", childList);

        return "member/main";
    }

    @GetMapping("/member/myPage")
    public String showMemberMyPage(Model model) {


        return "member/myPage";
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
            rttr.addFlashAttribute("msg", "정보가 수정되었습니다.");
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
