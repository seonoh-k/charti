package com.example.demo.users.controller;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.AddressService;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.*;
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
    private final AuthService authService;
    private final AddressService addressService;

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

    @GetMapping("/expert/myPage")
    public String showExpertMyPage(Model model) {
        log.info("[GET] 👨‍💼 request manager Page");
        UserDTO userDTO = authService.getLoginUser();

        AddressDTO address = addressService.getByExpertUid(userDTO.getUuid());

        userDTO.setAddress(address);
        model.addAttribute("userInfo", userDTO);

        return "expert/myPage";
    }


    @GetMapping("/admin/expert-applicants")
    public String showExpertApplicants(
            @ModelAttribute PagingRequest pagingRequest,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ExpertDTO, Expert> result =
                (type != null && keyword != null && !keyword.isBlank())
                        ? expertService.searchUnapprovedExpertList(type, keyword, pageable)
                        : expertService.getUnapprovedExpertList(pageable);

        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("result", result);

        return "admin/expertApplicants";
    }

    @GetMapping("/admin/expert-applicants/search")
    public ResponseEntity<ApiResponse<?>> searchExpertApplicants(
            @RequestParam String type,
            @RequestParam String keyword,
            @ModelAttribute PagingRequest pagingRequest) {

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ExpertDTO, Expert> result = expertService.searchUnapprovedExpertList(type, keyword, pageable);

        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @PostMapping("/admin/approve-expert")
    public String approveExpert(@ModelAttribute PagingRequest pagingRequest,
                                @RequestParam List<Long> ids ,
                                Model model) throws FirebaseAuthException{

        ids.forEach(firebaseService::setFirebaseMemberRoleToExpert);

        log.info(pagingRequest.getSort());

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ExpertDTO, Expert> result = expertService.getUnapprovedExpertList(pageable);
        ids.forEach(log::info);

        model.addAttribute("result",result);


        return "redirect:/admin/expert-applicants";
    }




}