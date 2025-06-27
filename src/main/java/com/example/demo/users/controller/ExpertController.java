package com.example.demo.users.controller;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.IdsRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.MatchingStatus;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.service.MatchingService;
import com.example.demo.service.AddressService;
import com.example.demo.exception.FirebaseAuthenticationException;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.*;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final MatchingService matchingService;

    @GetMapping("/expert/main")
    public String showExpertPage(Model model) {
        UserDTO user = authService.getLoginUser();
        List<Matching> totalMatching = matchingService.findAllById(user.getId());

        int matchingCount = 0;
        int endCount = 0;

        for(Matching matching : totalMatching) {
            if(matching.getStatus().equals(MatchingStatus.MATCHED)) {
                matchingCount++;
            }
            if(matching.getStatus().equals(MatchingStatus.RESPONDED)) {
                endCount++;
            }
        }

        int totalCount = totalMatching.size();

        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "createdAt");
        Page<Matching> matching = matchingService.findByExpertIdAndStatus(user.getId(), MatchingStatus.MATCHED, pageable);

        List<Matching> matchingList = matching.getContent();
        model.addAttribute("matchingList", matchingList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("matchingCount", matchingCount);
        model.addAttribute("endCount", endCount);

        return "/expert/expert";
    }

    @GetMapping("/expert/myPage")
    public String showExpertMyPage(Model model) {
        log.info("[GET] 👨‍💼 request manager Page");
        UserDTO userDTO = authService.getLoginUser();

        AddressDTO address = addressService.getAddressByUid(userDTO.getUuid());

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
    public ResponseEntity<ApiResponse> approveExpert(@ModelAttribute PagingRequest pagingRequest,
                                @RequestBody IdsRequest ids ,
                                @RequestParam(required = false) String type,
                                @RequestParam(required = false) String keyword,
                                RedirectAttributes redirectAttributes,
                                Model model) throws FirebaseAuthException{

        // 리스트 순회 -> 승인
        // 1. 파이어 베이스 클레임 변경
        // 2. 데이터베이스 정보 변경
        // 3. 성공 시 실패에 따라 응답 코드 발생
        // 5. 모달 창 띄워줌
        try{
            for (Long id : ids.getIds()){
                log.info("🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥ID : {} 🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥",id);
                firebaseService.setRoleToExpertInClaim(id);
                expertService.approveExpert(id);
            }
        } catch (UserNotFoundException userNotFoundException){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(AuthStatus.USER_NOT_FOUND));
        } catch (FirebaseAuthenticationException firebaseAuthenticationException){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(GlobalStatus.FIREBASE_ERROR));
        }

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ExpertDTO, Expert> result = expertService.getUnapprovedExpertList(pageable);

        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("result",result);


        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(GlobalStatus.OK));
    }

}