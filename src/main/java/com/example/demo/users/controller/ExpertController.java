package com.example.demo.users.controller;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.UserRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ExpertController {

    private final ExpertService expertService;
    private final FirebaseService firebaseService;
    private final ExpertRepository expertRepository;
    private final UserRepository usersRepository;

//    @GetMapping("/expert")
//    public String showExpertPage() {
//        log.info("[GET] 👨‍💼 request expert Page");
//        return "expert";
//    }
@GetMapping("/expert")
public String showExpertPage(Model model) {
    String uuid = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    log.info("===============================uuid : " + uuid);

    Users user = usersRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("해당 사용자 없음"));

    Optional<Expert> optionalExpert = expertRepository.findByUsersId(user.getId());

    if (optionalExpert.isPresent()) {
        Expert expert = optionalExpert.get();
        String licenseUrl = "/api/proxy/image?filename=" + URLEncoder.encode(expert.getLicense(), StandardCharsets.UTF_8);
        model.addAttribute("licenseImageUrl", licenseUrl);
        log.info("===============================licenseUrl : " + licenseUrl);
    } else {
        model.addAttribute("licenseImageUrl", null); // 또는 기본 이미지 경로 등
        log.info("===============================전문가 정보 없음, 기본 이미지 적용");
    }

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