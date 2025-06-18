package com.example.demo.users.controller;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.service.ExpertService;
import com.example.demo.users.service.FirebaseService;
import com.example.demo.util.GlobalStatus;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
public class ExpertController {

    private final ExpertService expertService;
    private final FirebaseService firebaseService;

    @GetMapping("/expert")
    public String showExpertPage() {
        log.info("[GET] 👨‍💼 request expert Page");
        return "expert";
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
    @GetMapping("/admin/expert-applicants")
    public String showAdminExpertApplicantsPage(@ModelAttribute PagingRequest pagingRequest, Model model) {
        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ExpertDTO, Expert> result = expertService.getPendingExpertListWithPaging(pageable);
        model.addAttribute("result",result);
        return "admin/expertApplicants"; // 뷰 파일
    }
    @PostMapping("/admin/approve-expert")
    public String approveExpert(@ModelAttribute PagingRequest pagingRequest,
                                @RequestParam List<Long> ids ,
                                Model model) throws FirebaseAuthException{

        ids.forEach(firebaseService::setFirebaseMemberRoleToExpert);

        log.info(pagingRequest.getSort());

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ExpertDTO, Expert> result = expertService.getPendingExpertListWithPaging(pageable);
        ids.forEach(log::info);

        model.addAttribute("result",result);


        return "redirect:/admin/expert-applicants";
    }




}