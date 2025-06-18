package com.example.demo.users.controller;

import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResponse;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.service.ManagerService;
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

    private final ManagerService managerService;


    @GetMapping("/manager")
    public String showMangerPage() {
        log.info("[GET] 👨‍💼 request manager Page");
        return "manager";
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
    public String showAdminManagerApplicantsPage(@ModelAttribute PagingRequest pagingRequest, Model model) {
        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ManagerDTO, Manager> result = managerService.getPendingManagerListWithPaging(pageable);
        model.addAttribute("result", result);
        log.info("Result : start Page {}" ,result.getStartPage());
        log.info("Result : Current Page {}" ,result.getPage());
        log.info("Result : End Page {}" ,result.getEndPage());
        log.info("Result : TotalPage {}" ,result.getTotalPages());
        return "admin/managerApplicants"; // 뷰 파일
    }
    @PostMapping("/admin/approve-manager")
    public String approveManager(@ModelAttribute PagingRequest pagingRequest, Model model){
        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ManagerDTO, Manager> result = managerService.getPendingManagerListWithPaging(pageable);
        model.addAttribute("result",result);


        return "redirect:/admin/manager-applicants";
    }


    @PostMapping("/api/admin/manager-applicants")
    public ResponseEntity<ApiResponse<PagingResponse<ManagerDTO>>> getPendingManagerList(@RequestBody PagingRequest request) {
        Pageable pageable = request.toPageable();
        PagingResultDTO<ManagerDTO, Manager> result = managerService.getPendingManagerListWithPaging(pageable);

        PagingResponse<ManagerDTO> response = PagingResponse.from(result);

        return ResponseEntity.ok(new ApiResponse<>(GlobalStatus.SUCCESS_WITH_DATA, response));
    }





}