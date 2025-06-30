package com.example.demo.users.controller;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.ExpertUpdateRequest;
import com.example.demo.dto.request.IdsRequest;
import com.example.demo.dto.request.ManagerUpdateRequest;
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
import org.springframework.security.core.Authentication;
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
        ExpertDTO expertDTO = expertService.getExpertByIdWithAddress(userDTO.getId());
        AddressDTO address = addressService.getAddressByUid(userDTO.getUuid());
        expertDTO.setAddress(address);
                model.addAttribute("userInfo", expertDTO);

        return "expert/myPage";
    }

    @PostMapping("/expert/update")
    public String updateManager(
            @ModelAttribute ExpertUpdateRequest req,
            Authentication authentication,
            RedirectAttributes rttr) {

        String uid = authentication.getPrincipal().toString();
        log.info("📞 /manager/update정보 이름 : {}", req.getName());
        log.info("📞 /manager/update정보 닉네임 : {}", req.getNickname());
        log.info("📞 /manager/update정보 전화번호 : {}", req.getPhoneNumber());
        try {
            expertService.updateExpert(req, uid);
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
        return "redirect:/expert/myPage";
    }




}