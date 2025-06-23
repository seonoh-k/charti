package com.example.demo.users.controller;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResponse;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ManagerService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ManagerController {

    private final AuthService authService;
    private final ManagerService managerService;
    private final UserService userService;
    private final AddressService addressService;


    @GetMapping("/manager")
    public String showMangerPage() {
        log.info("[GET] 👨‍💼 request manager Page");
        return "manager";
    }

    @GetMapping("/manager/myPage")
    public String showMangerMyPage(Model model) {
        log.info("[GET] 👨‍💼 request manager Page");
        UserDTO userDTO = authService.getLoginUser();

        AddressDTO address = addressService.getGroupIdByManagerUid(userDTO.getUuid());

        userDTO.setAddress(address);
        model.addAttribute("userInfo", userDTO);

        return "manager/myPage";
    }


    /**
     * GET /api/admin/managers/pending
     * localhost:8080/api/admin/managers/pending?page=0&size=5
     *
     * <ul>
     *     <li>페이지 :  현재 페이지</li>
     *     <li>페이지 하나당 보여줄 요소 개수</li>
     *     <li>정렬 기준 필드를 기준</li>
     *     <li>방향 [asc, desc]</li>
     * </ul>
     * @return
     */
    @GetMapping("/admin/manager-applicants")
    public String showAdminManagerApplicantsPage(@ModelAttribute PagingRequest pagingRequest,
                                                 @RequestParam(required = false) String type,
                                                 @RequestParam(required = false) String keyword,
                                                 Model model) {

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ManagerDTO, Manager> result =
                (type != null && keyword != null && !keyword.isBlank())
                        ? managerService.searchUnapprovedManagerList(type, keyword, pageable)
                        : managerService.getUnapprovedManagerList(pageable);

        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("result", result);

        return "admin/managerApplicants"; // 뷰 파일
    }
    @GetMapping("/admin/manager-applicants/search")
    public ResponseEntity<ApiResponse<?>> searchExpertApplicants(
            @RequestParam String type,
            @RequestParam String keyword,
            @ModelAttribute PagingRequest pagingRequest) {

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ManagerDTO, Manager> result = managerService.searchUnapprovedManagerList(type, keyword, pageable);

        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }
    @PostMapping("/admin/approve-manager")
    public String approveManager(@ModelAttribute PagingRequest pagingRequest, Model model){
        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ManagerDTO, Manager> result = managerService.getUnapprovedManagerList(pageable);
        model.addAttribute("result",result);


        return "redirect:/admin/manager-applicants";
    }


    @PostMapping("/api/admin/manager-applicants")
    public ResponseEntity<ApiResponse<PagingResponse<ManagerDTO>>> getPendingManagerList(@RequestBody PagingRequest request) {
        Pageable pageable = request.toPageable();
        PagingResultDTO<ManagerDTO, Manager> result = managerService.getUnapprovedManagerList(pageable);

        PagingResponse<ManagerDTO> response = PagingResponse.from(result);

        return ResponseEntity.ok(new ApiResponse<>(GlobalStatus.SUCCESS_WITH_DATA, response));
    }





}