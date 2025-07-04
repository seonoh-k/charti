package com.example.demo.users.controller;

import com.example.demo.dto.*;
import com.example.demo.dto.request.ChildCreateRequest;
import com.example.demo.dto.request.ChildUpdateRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.fcm.dto.NoticeDto;
import com.example.demo.fcm.service.FcmService;
import com.example.demo.service.AddressService;
import com.example.demo.service.PointService;
import com.example.demo.survey.dto.DailyAnswerDto;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.survey.service.DailyAnswerService;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.example.demo.users.service.UserService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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
    private final PointService pointService;
    private final FcmService fcmService;

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
            case ROLE_EXPERT -> "redirect:/expert/main";
            case ROLE_MANAGER -> "redirect:/manager/main";
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

    @GetMapping("/member/notice")
    public String noticeHistory(Model model) {
        UserDTO user = authService.getLoginUser();

        Users currentUser = userService.dtoToEntity(user);
        List<NoticeDto> noticeList = fcmService.getNotices(currentUser);

        model.addAttribute("noticeList", noticeList);
        return "member/noticeHistory";
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

    @GetMapping("/member/child/{childId}")
    @ResponseBody
    public ChildDTO getChildDetail(@PathVariable Long childId) {
        Child child = childService.findById(childId);

        return ChildDTO.fromEntityWithDetails(child);
    }
    @PostMapping("/member/child/create")
    public String addChild(@ModelAttribute @Valid ChildCreateRequest dto, BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            // 에러 메시지 리스트 만들기
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .toList();
            ra.addFlashAttribute("error", errorMessages);
            return "redirect:/member/child";
        }
        // 날짜확인
        if (dto.getBirthday() != null) {
            LocalDate date = LocalDate.parse(dto.getBirthday());
            if (date.isAfter(LocalDate.now())) {
                ra.addFlashAttribute("error", "생일은 오늘보다 이후일 수 없습니다.");
                return "redirect:/member/child";
            }
        }
        childService.createChild(dto);
        ra.addFlashAttribute("msg", "자녀가 등록되었습니다!");
        return "redirect:/member/child";
    }
    @PostMapping("/member/child/update")
    public String updateChild(
            @ModelAttribute @Valid ChildUpdateRequest dto,
            BindingResult bindingResult,
            RedirectAttributes ra
    ) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .toList();
            ra.addFlashAttribute("error", errorMessages);
            return "redirect:/member/child";
        }
        // 생일 오늘 이후 체크
        if (dto.getBirthday() != null && dto.getBirthday().isAfter(LocalDate.now())) {
            ra.addFlashAttribute("error", "생일은 오늘보다 이후일 수 없습니다.");
            return "redirect:/member/child";
        }
        childService.updateChild(dto.getId(), dto);
        ra.addFlashAttribute("msg", "수정 완료!");
        return "redirect:/member/child";
    }
    @PostMapping("/member/child/delete")
    public String deleteChild(@RequestParam Long id, RedirectAttributes ra) {
        childService.softDeleteChild(id);
        ra.addFlashAttribute("msg", "삭제 완료!");
        return "redirect:/member/child";
    }



    @PostMapping("/member/update")
    public String updateMemberInfo(
            @Valid @ModelAttribute UserUpdateRequest req,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes rttr) {

        // 1. 유효성 검사 실패 시 바로 리다이렉트 + 에러메시지 전달
        if (bindingResult.hasErrors()) {
            // 모든 에러 메시지 중 첫번째 에러만 전달
            String msg = bindingResult.getFieldError() != null ?
                    bindingResult.getFieldError().getDefaultMessage() : "입력값을 확인해주세요.";
            rttr.addFlashAttribute("error", msg);
            return "redirect:/member/myPage";
        }

        // 1. 인증된 유저 정보 확인
        String uid = authentication.getPrincipal().toString();

        // 3. 서비스 호출 (실제 정보 수정)
        try {
            userService.updateMember(req, uid);
            rttr.addFlashAttribute("msg", "정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "수정 중 오류 발생: " + e.getMessage());
        }

        // 4. 리다이렉트(페이지 새로고침)
        return "redirect:/member/myPage";
    }

    @GetMapping("/member/surveyHistory")
    public String surveyHistory() { return "member/surveyHistory"; }

    @GetMapping("/member/pointHistory")
    public String pointHistory(Model model) {

        UserDTO userDTO = authService.getLoginUser();

        int point = pointService.getCurrentPoint(userDTO.getId());
        List<PointHistoryView> pointHistory = pointService.getFormattedHistory(userDTO.getId());

        model.addAttribute("point", point);
        model.addAttribute("pointHistory", pointHistory);

        return "member/pointHistory";
    }

//    @GetMapping("/main")
//    public String showMainPage(Model model){
//
//        log.info("[GET] 🟢 AuthController.showMainPage");
//
////        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        String uuid = (String) authentication.getPrincipal();
////        log.info(uuid);
//
////        String role = authentication.getAuthorities()
////                .stream()
////                .findFirst()
////                .get()
////                .toString();
//
//        try{
//
//            UserDTO userDTO = authService.getLoginUser();
//            model.addAttribute("email", userDTO.getName());
//            model.addAttribute("role",userDTO.getRole());
//
//        } catch (UserNotFoundException e){
//            log.info(" ⚠ AuthController.showMainPage MemberNotFoundException ");
//        }
//        return "main";
//
//    }

}
