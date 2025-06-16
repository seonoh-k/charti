package com.example.demo.users.controller;

import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.request.PagingRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.users.service.ExpertService;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ExpertController {

    private final ExpertService expertService;

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
    public String showAdminExpertPendingPage(@ModelAttribute PagingRequest pagingRequest , Model model) {
        log.info("[GET] 👨‍💼 request manager Page");

        Pageable pageable = pagingRequest.toPageable();
        List<ManagerDTO> pendingManagerList = expertService.getPendingExpertList(pageable);

        // 쿼리 파라미터 그대로 뷰에 전달
        model.addAttribute("page", pagingRequest.getPage());
        model.addAttribute("size", pagingRequest.getSize());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("direction", pagingRequest.getDirection());

        model.addAttribute("pendingManagerList",pendingManagerList);

        return "/admin/manager/pendingManagerList";
    }

    @PostMapping("/api/admin/expert-applicants")
    public ResponseEntity<ApiResponse> getPendingExpertList(@RequestBody PagingRequest pagingRequest) {
        Pageable pageable = pagingRequest.toPageable();

        List<ManagerDTO> result = expertService.getPendingExpertList(pageable);

        if(result.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse(GlobalStatus.SUCCESS_WITH_DATA,result));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(GlobalStatus.SUCCESS_WITH_DATA,result));

    }


}
