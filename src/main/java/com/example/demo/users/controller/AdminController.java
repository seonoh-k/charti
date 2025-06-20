package com.example.demo.users.controller;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.users.entity.*;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.ExpertService;
import com.example.demo.users.service.ManagerService;
import com.example.demo.users.service.MemberService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.GlobalStatus;
import com.example.demo.util.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class AdminController {

    private final ExpertService expertService;
    private final MemberService memberService;
    private final ManagerService managerService;
    private final UserService userService;

    @GetMapping("/admin")
    public String showAdminPage() {
        log.info("[GET] 👨‍💼 request Admin Page");
        return "admin";
    }
    @GetMapping("/admin/main")
    public String showAdminMainPage(Model model) {
        log.info("[GET] 👨‍💼 request Admin Main Page");

        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(0, 5, sort);

        List<ManagerDTO> managerDTOList = managerService.getManagerList(pageable);
        List<ExpertDTO> expertDTOList = expertService.getExpertList(pageable);
        List<MemberDTO> memberDTOList = userService.getMemberList(pageable);
        // List<MemberDTO> memberDTOList = memberService.getMemberList(pageable);

        model.addAttribute("managerDTOList",managerDTOList);
        model.addAttribute("expertDTOList",expertDTOList);
        model.addAttribute("memberDTOList",memberDTOList);

        return "admin/main";
    }

    @GetMapping("/admin/member/all")
    public String showAdminMemberListPage(@ModelAttribute PagingRequest pagingRequest,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String keyword,
                                          Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<MemberDTO, Users> result = (type != null && keyword != null && !keyword.isBlank())
                        ? userService.getMemberListWithPaging(type, keyword, pageable)
                        : userService.getMemberListWithPaging(pageable);

        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("result",result);

        return "admin/member/memberList"; // 뷰 파일
    }
    @GetMapping("/admin/expert/all")
    public String showAdminExpertListPage(@ModelAttribute PagingRequest pagingRequest,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String keyword,
                                          Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ExpertDTO, Users> result =
                (type != null && keyword != null && !keyword.isBlank())
                        ? userService.getPendingExpertListWithPaging(type, keyword, pageable)
                        : userService.getPendingExpertListWithPaging(pageable);

        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("result",result);

        return "admin/expert/expertList"; // 뷰 파일
    }
    @GetMapping("/admin/manager/all")
    public String showAdminManagerListPage(@ModelAttribute PagingRequest pagingRequest,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(required = false) String keyword,
                                           Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ManagerDTO, Users> result =
                (type != null && keyword != null && !keyword.isBlank())
                        ? userService.getPendingManagerListWithPaging(type, keyword, pageable)
                        : userService.getPendingManagerListWithPaging(pageable);

        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("result", result);

        return "admin/manager/managerList"; // 뷰 파일
    }

    @GetMapping("/admin/member/all/search")
    public ResponseEntity<ApiResponse> searchAdminMemberListPage(@ModelAttribute PagingRequest pagingRequest,
                                                                 @RequestParam(required = false) String type,
                                                                 @RequestParam(required = false) String keyword,
                                                                 Model model){
        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<MemberDTO, Users> result = userService.getMemberListWithPaging(type, keyword, pageable);


        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @GetMapping("/admin/expert/all/search")
    public ResponseEntity<ApiResponse> searchAdminExpertListPage(@ModelAttribute PagingRequest pagingRequest,
                                                                 @RequestParam(required = false) String type,
                                                                 @RequestParam(required = false) String keyword,
                                                                 Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ExpertDTO, Users> result = userService.getPendingExpertListWithPaging(type,keyword,pageable);

        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @GetMapping("/admin/manager/all/search")
    public ResponseEntity<ApiResponse> searchAdminManagerListPage(@ModelAttribute PagingRequest pagingRequest,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) String keyword,
                                             Model model){

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ManagerDTO, Users> result = userService.getPendingManagerListWithPaging(type, keyword, pageable);

        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @GetMapping("/admin/member/{id:[0-9]+}")
    public String showAdminMemberUpdatePage(@ModelAttribute PagingRequest pagingRequest,
                                            @PathVariable Long id,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        MemberDTO memberDTO;

        try{
            memberDTO = userService.findMemberById(id);
            log.info(memberDTO.getCreatedAt());
        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/member/all";
        }

        model.addAttribute("memberDTO",memberDTO);

        return "admin/member/updateForm";
    }




    @GetMapping("/admin/expert/{id:[0-9]+}")
    public String showAdminExpertUpdatePage(@ModelAttribute PagingRequest pagingRequest,
                                            @PathVariable Long id,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        ExpertDTO expertDTO;

        try{
            expertDTO = userService.findExpertById(id);

        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/expert/all";
        }

        model.addAttribute("expertDTO",expertDTO);

        return "admin/expert/updateForm";
    }
    @GetMapping("/admin/manager/{id:[0-9]+}")
    public String showAdminManagerUpdatePage(@ModelAttribute PagingRequest pagingRequest,
                                            @PathVariable Long id,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        ManagerDTO managerDTO;

        try{
            managerDTO = userService.findManagerById(id);

        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/manager/all";
        }

        model.addAttribute("managerDTO",managerDTO);

        return "admin/manager/updateForm";
    }

    @PostMapping("/admin/member/{id:[0-9]+}")
    public ResponseEntity<ApiResponse> updateMember(@RequestBody MemberDTO memberDTO,
                                                    @ModelAttribute PagingRequest pagingRequest,
                                                    RedirectAttributes redirectAttributes) {
        // 수정 하기
        // userService.updateMember(memberDTO);
        log.info(memberDTO);
        // 수정 성공 후 페이징 위치 유지하며 리다이렉트
        redirectAttributes.addAttribute("page", pagingRequest.getPage());
        redirectAttributes.addAttribute("size", pagingRequest.getSize());
        redirectAttributes.addAttribute("sort", pagingRequest.getSort());
        redirectAttributes.addAttribute("direction", pagingRequest.getDirection());

        // 권한 없음 관리자가 아닌 사용자가 수정 시도 ACCESS_DENIED
        // 인증 실패 로그인 상태 아님, 만료된 세션	AUTHENTICATION_FAIL
        // ID 미존재 	존재하지 않는 회원을 수정 시도 ENTITY_NOT_FOUND
        // JWT 토큰 오류 JWT_VALIDATION_FAIL

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(GlobalStatus.OK));
    }
    @PostMapping("/admin/expert/{id:[0-9]+}")
    public ResponseEntity<ApiResponse> updateExpert(@RequestBody ExpertDTO expertDTO,
                                                    @ModelAttribute PagingRequest pagingRequest,
                                                    RedirectAttributes redirectAttributes) {
        // 수정 하기
        // userService.updateMember(memberDTO);
        log.info(expertDTO);
        // 수정 성공 후 페이징 위치 유지하며 리다이렉트
        redirectAttributes.addAttribute("page", pagingRequest.getPage());
        redirectAttributes.addAttribute("size", pagingRequest.getSize());
        redirectAttributes.addAttribute("sort", pagingRequest.getSort());
        redirectAttributes.addAttribute("direction", pagingRequest.getDirection());

        // 권한 없음 관리자가 아닌 사용자가 수정 시도 ACCESS_DENIED
        // 인증 실패 로그인 상태 아님, 만료된 세션	AUTHENTICATION_FAIL
        // ID 미존재 	존재하지 않는 회원을 수정 시도 ENTITY_NOT_FOUND
        // JWT 토큰 오류 JWT_VALIDATION_FAIL

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(GlobalStatus.OK));
    }
    @PostMapping("/admin/manager/{id:[0-9]+}")
    public ResponseEntity<ApiResponse> updateManager(@RequestBody ManagerDTO managerDTO,
                                                    @ModelAttribute PagingRequest pagingRequest,
                                                    RedirectAttributes redirectAttributes) {
        // 수정 하기
        // userService.updateMember(memberDTO);
        log.info(managerDTO);
        // 수정 성공 후 페이징 위치 유지하며 리다이렉트
        redirectAttributes.addAttribute("page", pagingRequest.getPage());
        redirectAttributes.addAttribute("size", pagingRequest.getSize());
        redirectAttributes.addAttribute("sort", pagingRequest.getSort());
        redirectAttributes.addAttribute("direction", pagingRequest.getDirection());

        // 권한 없음 관리자가 아닌 사용자가 수정 시도 ACCESS_DENIED
        // 인증 실패 로그인 상태 아님, 만료된 세션	AUTHENTICATION_FAIL
        // ID 미존재 	존재하지 않는 회원을 수정 시도 ENTITY_NOT_FOUND
        // JWT 토큰 오류 JWT_VALIDATION_FAIL

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(GlobalStatus.OK));
    }




}