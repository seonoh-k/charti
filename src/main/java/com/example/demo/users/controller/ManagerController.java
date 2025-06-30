package com.example.demo.users.controller;

import com.example.demo.dto.*;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResponse;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.IdsRequest;
import com.example.demo.dto.request.ManagerUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.fcm.dto.FcmHistorySearchDto;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.service.FcmHistoryService;
import com.example.demo.service.AddressService;
import com.example.demo.exception.FirebaseAuthenticationException;
import com.example.demo.service.GroupService;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.*;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.GlobalStatus;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ManagerController {

    private final AuthService authService;
    private final ManagerService managerService;
    private final UserService userService;
    private final AddressService addressService;
    private final FirebaseService firebaseService;
    private final ChildService childService;
    private final GroupService groupService;
    private final FcmHistoryService historyService;

    @GetMapping("/manager")
    public String showMangerPage() {
        log.info("[GET] 👨‍💼 request manager Page");
        return "manager";
    }
    @GetMapping("/manager/home")
    public String showMangerHome(Model model) {
        log.info("[GET] 👨‍💼 request managerHome");
        // 현재 로그인한 담당자 id
        UserDTO loginUser = authService.getLoginUser();
        // 담당자 관련 요약정보
        ManagerDashboardDto dashboardInfo = managerService.getDashboardInfo(loginUser.getId());
        // 문진이력 검색조건 ,        5개까지만
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "sentAt"));
        FcmHistorySearchDto searchDto = new FcmHistorySearchDto();
        searchDto.setSenderName(loginUser.getName());

        List<FcmSendHistory> recentNotices = historyService.getRecentGroupNotices(loginUser.getName(), 5);


        // Model에 정보 담기
        model.addAttribute("dashboardInfo", dashboardInfo);
        model.addAttribute("recentNotices", recentNotices);

        return "manager/managerHome";
    }

    @GetMapping("/manager/myPage")
    public String showManagerMyPage(Model model) {
        log.info("[GET] 👨‍💼 request manager Page");

        UserDTO loginUser = authService.getLoginUser(); // 유저 전체 정보
        AddressDTO address = addressService.getAddressByUid(loginUser.getUuid());
        ManagerDTO  managerDTO = managerService.getManagerById(loginUser.getId());
        managerDTO.setAddress(address);

        log.info("📞 managerDTO.getGroupEmail = {}", managerDTO.getGroupEmail());
        log.info("📞 managerDTO.getPhoneNumber = {}", managerDTO.getPhoneNumber());
        log.info("📞 managerDTO.getGroupId = {}", managerDTO.getGroupId());
        model.addAttribute("userInfo", managerDTO);

        return "manager/myPage";
    }

    @GetMapping("/api/group/{groupId}/parent-cards")
    public ResponseEntity<Page<ParentWithChildrenDTO>> getParentCards(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<ParentWithChildrenDTO> parentCards = groupService.getParentCardsByGroup(groupId, page, size);
        return ResponseEntity.ok(parentCards);
    }
    @GetMapping("/manager/groupManager")
    public String showGroupManagerPage(Model model) {
        log.info("[GET] 👨‍💼 request groupManager Page");

        //  로그인 유저, 그룹, 담당자 등 기존 정보 세팅
        UserDTO loginUser = authService.getLoginUser();
        AddressDTO address = addressService.getAddressByUid(loginUser.getUuid());
        ManagerDTO managerDTO = managerService.getManagerById(loginUser.getId());
        managerDTO.setAddress(address);

        model.addAttribute("groupInfo", managerDTO);

        return "manager/groupManager";
    }
    @PostMapping("/manager/update")
    public String updateManager(
            @ModelAttribute ManagerUpdateRequest req,
            Authentication authentication,
            RedirectAttributes rttr) {

        String uid = authentication.getPrincipal().toString();
        log.info("📞 /manager/update정보 이름 : {}", req.getName());
        log.info("📞 /manager/update정보 닉네임 : {}", req.getNickname());
        log.info("📞 /manager/update정보 전화번호 : {}", req.getPhoneNumber());
        try {
            managerService.updateManager(req, uid);
            rttr.addFlashAttribute("msg", "정보가 성공적으로 수정되었습니다.");
        } catch (FirebaseAuthException e) {
            // Firebase  업데이트 실패 시
            log.error("❌ Firebase 업데이트 실패: {}", e.getMessage(), e);
            rttr.addFlashAttribute("error", "Firebase 업데이트 실패: " + "정보 수정 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요");
        } catch (Exception e) {
            // 기타 예외 처리 (Optional)
            log.error("❌ 정보 수정 중 오류 발생", e);
            rttr.addFlashAttribute("error", "정보 수정 중 오류가 발생했습니다.");
        }
        return "redirect:/manager/myPage";
    }
    @PostMapping("/manager/group/update")
    public String updateGroup(
            @ModelAttribute ManagerUpdateRequest req,
            Authentication authentication,
            RedirectAttributes rttr) {

        String uid = authentication.getPrincipal().toString();
        log.info("📞 /manager/update정보 주소id : {}", req.getAddressId());
        log.info("📞 /manager/update정보 그룹이름 : {}", req.getGroupName());
        log.info("📞 /manager/update정보 그룹 전화번호 : {}", req.getGroupPhoneNumber());
        log.info("📞 /manager/update정보 그룹이메일 : {}", req.getGroupEmail());
        log.info("📞 /manager/update정보 그룹 분류 : {}", req.getTargetGroup());
        try {
            managerService.updateManager(req, uid);
            rttr.addFlashAttribute("msg", "정보가 성공적으로 수정되었습니다.");
        } catch (FirebaseAuthException e) {
            // Firebase  업데이트 실패 시
            log.error("❌ Firebase 업데이트 실패: {}", e.getMessage(), e);
            rttr.addFlashAttribute("error", "Firebase 업데이트 실패: " + "정보 수정 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요");
        } catch (Exception e) {
            // 기타 예외 처리 (Optional)
            log.error("❌ 정보 수정 중 오류 발생", e);
            rttr.addFlashAttribute("error", "정보 수정 중 오류가 발생했습니다.");
        }
        return "redirect:/manager/groupManager";
    }

    @PostMapping("/manager/child/remove")
    @ResponseBody
    public Map<String, Object> removeChildFromGroup(@RequestBody Map<String, Long> req) {
        Long childId = req.get("childId");
        childService.removeChildFromGroup(childId);
        return Map.of("message", "자녀가 그룹에서 제외되었습니다.");
    }









}