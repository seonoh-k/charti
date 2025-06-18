package com.example.demo.users.controller;

import com.example.demo.dto.ManagerDTO;

import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.users.service.ManagerService;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    public String showAdminManagerPendingPage(@ModelAttribute PagingRequest pagingRequest , Model model) {
        log.info("[GET] 👨‍💼 request manager Page");

        Pageable pageable = pagingRequest.toPageable();
        List<ManagerDTO> pendingManagerList = managerService.getPendingManagerList(pageable);

        // 쿼리 파라미터 그대로 뷰에 전달
        model.addAttribute("page", pagingRequest.getPage());
        model.addAttribute("size", pagingRequest.getSize());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("direction", pagingRequest.getDirection());

        model.addAttribute("pendingManagerList",pendingManagerList);

        return "admin/managerApplicants";
    }

    @PostMapping("/api/admin/manager-applicants")
    public ResponseEntity<ApiResponse> getPendingManagerList(@RequestBody PagingRequest pagingRequest) {
        Pageable pageable = pagingRequest.toPageable();

        List<ManagerDTO> result = managerService.getPendingManagerList(pageable);

        if(result.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse(GlobalStatus.SUCCESS_WITH_DATA,result));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(GlobalStatus.SUCCESS_WITH_DATA,result));

    }



}
