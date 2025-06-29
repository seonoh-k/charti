package com.example.demo.users.controller;

import com.example.demo.annotation.AdminActionHistoryAuditLog;
import com.example.demo.dto.*;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResponse;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.AdminActionHistoryCategory;
import com.example.demo.exception.FirebaseAuthenticationException;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.users.entity.*;
import com.example.demo.users.exception.AdminAlreadyExistsException;
import com.example.demo.users.exception.AdminNotFoundException;
import com.example.demo.users.exception.UserIsNotDeletedException;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.*;
import com.example.demo.util.AuthStatus;
import com.example.demo.util.GlobalStatus;
import com.example.demo.util.UserStatus;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Controller
@RequiredArgsConstructor
@Log4j2
public class AdminController {

    private final AdminService adminService;
    private final ExpertService expertService;
    private final MemberService memberService;
    private final ManagerService managerService;
    private final UserService userService;
    private final ChildService childService;
    private final FirebaseService firebaseService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;



    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.TEST)
    @GetMapping("/admin")
    public String showAdminPage() {
        log.info("[GET] 👨‍💼 request Admin Page");
        return "admin";
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_ADMIN_MAIN)
    @GetMapping("/admin/main")
    public String showAdminMainPage(Model model) {
        log.info("[GET] 👨‍💼 request Admin Main Page");

        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(0, 5, sort);

        List<ManagerDTO> managerDTOList = managerService.getLatestUnapproving(pageable);
        List<ExpertDTO> expertDTOList = expertService.getLatestUnapproving(pageable);
        List<MemberDTO> memberDTOList = memberService.getLatestSignups(pageable);

        model.addAttribute("managerDTOList",managerDTOList);
        model.addAttribute("expertDTOList",expertDTOList);
        model.addAttribute("memberDTOList",memberDTOList);

        return "admin/main";
    }
    @GetMapping("/admin/loginForm")
    public String showAdminLoginPage() {
        log.info("[GET] 👨‍💼 request Admin Main Page");
        return "admin/loginForm";
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_ADMIN_CREATE_FORM)
    @GetMapping("/admin/create/admin")
    public String showCreateAdminPage(Model model) {
        log.info("[GET] 👨‍💼 request Create Admin Page");

        return "admin/admin/createAdminForm";
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.CHECK_ADMIN_VALIDATION)
    @PostMapping("/admin/check/admin")
    public ResponseEntity<ApiResponse> checkAdminValidation(@Valid @RequestBody AdminCreateRequest request,
                                                   BindingResult bindingResult,
                                                   Model model) {
        log.info("[POST] 👨‍💼 request Create Admin Page");
        // 중복검사
        // 1. 암호화
        // 2.
        String email = request.getUsername();
        log.info("전화번호 : {}",request.getPhoneNumber());
        List<ErrorDetail> errors = new ArrayList<ErrorDetail>();

        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            errors = fieldErrors.stream()
                    .map(fe -> new ErrorDetail(fe.getField(), fe.getDefaultMessage()))
                    .collect(Collectors.toList());

            // ⇒ 여러 개의 FieldError를 한꺼번에 가져옴 :contentReference[oaicite:2]{index=2}
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.responseError(GlobalStatus.VALIDATION_FAIL, errors));
        }

        boolean adminIsExistsInDatabase = adminService.existsAdminByUsername(email);
        boolean adminIsExistsInFirebase = firebaseService.existsByEmail(email);

        if (adminIsExistsInDatabase || adminIsExistsInFirebase){
            errors.add(new ErrorDetail("username","사용할 수 없는 아이디에요"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.responseError(GlobalStatus.USERNAME_VALIDATION_FAILED,errors));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(GlobalStatus.OK));
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.CREATE_ADMIN)
    @PostMapping("/admin/create/admin")
    public ResponseEntity<ApiResponse> createAdmin(@RequestBody AdminCreateRequest request,
                                                   Model model) {
        try{
            // 파이어 베이스 계정 생성
            String uuid = firebaseService.createAdmin(request);
            request.setUuid(uuid);

            boolean isExistInDB = userService.existsByEmail(request.getUsername());

            if (isExistInDB){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(GlobalStatus.USERNAME_VALIDATION_FAILED));
            }

            // Database 계정 생성
            adminService.createAdmin(request);

            // Claim에 Admin 생성
            firebaseService.setRoleToAdminInClaim(request.getId());

        } catch (AdminAlreadyExistsException aaee){
            log.info("🔥🔥🔥🔥🔥 request.getUsername() : {}",request.getUsername());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(GlobalStatus.USERNAME_VALIDATION_FAILED));
        } catch (FirebaseAuthException fe){
            // Firebase에 계정 생성하다가 실패 시 발생
            // PHONE_NUMBER_ALREADY_EXISTS :
            // EMAIL_ALREADY_EXISTS
            if (AuthErrorCode.EMAIL_ALREADY_EXISTS == fe.getAuthErrorCode()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(GlobalStatus.USERNAME_VALIDATION_FAILED));
            } else if(AuthErrorCode.PHONE_NUMBER_ALREADY_EXISTS == fe.getAuthErrorCode()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(GlobalStatus.PHONENUMBER_VALIDATION_FAILED));
            } else{
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(GlobalStatus.FIREBASE_ERROR));
            }

        } catch (FirebaseAuthenticationException fae){
            // Role Claims에 저장하다가 실패 시 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(GlobalStatus.FIREBASE_ERROR));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(GlobalStatus.OK));
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_USERS_DELETED_LIST)
    @GetMapping("/admin/users/all/deleted")
    public String showAdminDeletedUserListPage(@ModelAttribute PagingRequest pagingRequest,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String keyword,
                                          Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<UserDTO, Users> result = (type != null && keyword != null && !keyword.isBlank())
                ? userService.searchDeletedUsers(type, keyword, pageable)
                : userService.getDeletedUserList(pageable);

        log.info("탈퇴 회원 수 {}" ,result.getTotalElements());
        log.info("회원 이 있죠? {} ", !result.getDtoList().isEmpty());

        model.addAttribute("hasDeletedUser",!result.getDtoList().isEmpty());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("direction", pagingRequest.getDirection());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("result",result);

        return "admin/users/deletedUserList"; // 뷰 파일
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_MEMBER_LIST)
    @GetMapping("/admin/member/all")
    public String showAdminMemberListPage(@ModelAttribute PagingRequest pagingRequest,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String keyword,
                                          Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<MemberDTO, Member> result = (type != null && keyword != null && !keyword.isBlank())
                        ? memberService.searchMemberList(type, keyword, pageable)
                        : memberService.getMemberList(pageable);
        model.addAttribute("hasMember", !result.getDtoList().isEmpty());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("direction", pagingRequest.getDirection());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("result",result);

        return "admin/member/memberList"; // 뷰 파일
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_EXPERT_LIST)
    @GetMapping("/admin/expert/all")
    public String showAdminExpertListPage(@ModelAttribute PagingRequest pagingRequest,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String keyword,
                                          Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO result =
                (type != null && keyword != null && !keyword.isBlank())
                        ? expertService.searchApprovedExpertList(type, keyword, pageable)
                        : expertService.getApprovedExpertList(pageable);

        model.addAttribute("hasExpert", !result.getDtoList().isEmpty());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("direction", pagingRequest.getDirection());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("result",result);

        return "admin/expert/expertList"; // 뷰 파일
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_MANAGER_LIST)
    @GetMapping("/admin/manager/all")
    public String showAdminManagerListPage(@ModelAttribute PagingRequest pagingRequest,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(required = false) String keyword,
                                           Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ManagerDTO, Manager> result =
                (type != null && keyword != null && !keyword.isBlank())
                        ? managerService.searchApprovedManagerList(type, keyword, pageable)
                        : managerService.getApprovedManagerList(pageable);

        model.addAttribute("hasManager", !result.getDtoList().isEmpty());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("direction", pagingRequest.getDirection());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("result", result);

        return "admin/manager/managerList"; // 뷰 파일
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_ADMIN_LIST)
    @GetMapping("/admin/admin/all")
    public String showAdminListPage(@ModelAttribute PagingRequest pagingRequest,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(required = false) String keyword,
                                           Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<AdminDTO, Admin> result =
                (type != null && keyword != null && !keyword.isBlank())
                        ? adminService.searchAdminList(type, keyword, pageable)
                        : adminService.getAdminList(pageable);

        model.addAttribute("hasAdmin", !result.getDtoList().isEmpty());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("direction", pagingRequest.getDirection());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("result", result);

        return "admin/admin/adminList"; // 뷰 파일
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.SEARCH_ADMIN)
    @GetMapping("/admin/admin/all/search")
    public ResponseEntity<ApiResponse> searchAdminListPage(@ModelAttribute PagingRequest pagingRequest,
                                                                      @RequestParam(required = false) String type,
                                                                      @RequestParam(required = false) String keyword,
                                                                      Model model){

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<AdminDTO, Admin> result = adminService.searchAdminList(type, keyword, pageable);
        model.addAttribute("sort", pagingRequest.getSort());
        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.SEARCH_USERS_DELETED)
    @GetMapping("/admin/users/all/deleted/search")
    public ResponseEntity<ApiResponse> searchAdminDeletedUserListPage(@ModelAttribute PagingRequest pagingRequest,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) String keyword,
                                               Model model){

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<UserDTO, Users> result = userService.searchDeletedUsers(type, keyword, pageable);
        model.addAttribute("sort", pagingRequest.getSort());
        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.SEARCH_MEMBER)
    @GetMapping("/admin/member/all/search")
    public ResponseEntity<ApiResponse> searchAdminMemberListPage(@ModelAttribute PagingRequest pagingRequest,
                                                                 @RequestParam(required = false) String type,
                                                                 @RequestParam(required = false) String keyword,
                                                                 Model model){
        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<MemberDTO, Member> result = memberService.searchMemberList(type, keyword, pageable);
        model.addAttribute("sort", pagingRequest.getSort());

        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.SEARCH_EXPERT)
    @GetMapping("/admin/expert/all/search")
    public ResponseEntity<ApiResponse> searchAdminExpertListPage(@ModelAttribute PagingRequest pagingRequest,
                                                                 @RequestParam(required = false) String type,
                                                                 @RequestParam(required = false) String keyword,
                                                                 Model model){

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO result = expertService.searchApprovedExpertList(type,keyword,pageable);

        model.addAttribute("sort", pagingRequest.getSort());

        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.SEARCH_MANAGER)
    @GetMapping("/admin/manager/all/search")
    public ResponseEntity<ApiResponse> searchAdminManagerListPage(@ModelAttribute PagingRequest pagingRequest,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) String keyword,
                                             Model model){

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ManagerDTO, Manager> result = managerService.searchApprovedManagerList(type, keyword, pageable);
        model.addAttribute("sort", pagingRequest.getSort());
        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }


        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_ADMIN_UPDATE_FORM)
    @GetMapping("/admin/admin/{id:[0-9]+}")
    public String showAdminUpdatePage(@ModelAttribute PagingRequest pagingRequest,
                                            @PathVariable Long id,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        AdminDTO adminDTO;

        try{
            adminDTO = adminService.getAdminDTOById(id);

        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/admin/all";
        }
        model.addAttribute("adminDTO",adminDTO);

        return "admin/admin/updateForm";
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_USERS_DELETED_RESTORE_FORM)
    @GetMapping("/admin/users/deleted/{id:[0-9]+}")
    public String showAdminDeletedUserUpdatePage(@PathVariable Long id,
                                                @ModelAttribute PagingRequest pagingRequest,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) String keyword,
                                               RedirectAttributes redirectAttribute,
                                                 Model model){
        UserDTO userDTO;

        Pageable pageable = pagingRequest.toPageable();

        try{
            userDTO = userService.getDeletedUserDTOById(id);
            model.addAttribute("activeUser",false);
            log.info(userDTO.getCreatedAt());
        } catch (UserNotFoundException userNotFoundException){

            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            model.addAttribute("activeUser",true);
            return "admin/users/deletedUserUpdateForm"; // 뷰 파일
            // return "redirect:/admin/users/all/deleted";
        }
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("userDTO",userDTO);

        return "admin/users/deletedUserUpdateForm"; // 뷰 파일
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_MEMBER_UPDATE_FORM)
    @GetMapping("/admin/member/{id:[0-9]+}")
    public String showAdminMemberUpdatePage(@ModelAttribute PagingRequest pagingRequest,
                                            @PathVariable Long id,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        MemberDTO memberDTO;

        try{
            memberDTO = memberService.getMemberById(id);

            log.info(memberDTO.getCreatedAt());
        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/member/all";
        }
        model.addAttribute("hasChildren", !memberDTO.getChildren().isEmpty());
        model.addAttribute("memberDTO",memberDTO);

        return "admin/member/updateForm";
    }



    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_EXPERT_UPDATE_FORM)
    @GetMapping("/admin/expert/{id:[0-9]+}")
    public String showAdminExpertUpdatePage(@PathVariable Long id,
                                            @ModelAttribute PagingRequest pagingRequest,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        ExpertDTO expertDTO;

        try{
            expertDTO = expertService.getExpertById(id);

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
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_MANAGER_UPDATE_FORM)
    @GetMapping("/admin/manager/{id:[0-9]+}")
    public String showAdminManagerUpdatePage(@ModelAttribute PagingRequest pagingRequest,
                                            @PathVariable Long id,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        ManagerDTO managerDTO;
        try{

            managerDTO = managerService.getManagerById(id);
            Pageable pageable = pagingRequest.toPageable();


        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/manager/all";
        }
        log.info("!managerDTO.getChildren().isEmpty() : {}",!managerDTO.getChildren().isEmpty());

        if(!managerDTO.getChildren().isEmpty()){
            managerDTO.getChildren().forEach((child)->log.info("child.getName() : {}", child.getName()));
        }
        model.addAttribute("hasChildren", !managerDTO.getChildren().isEmpty()); // 값이 있으면
        model.addAttribute("managerDTO",managerDTO);

        return "admin/manager/updateForm";
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.UPDATE_MEMBER)
    @PostMapping("/admin/member/{id:[0-9]+}")
    public ResponseEntity<ApiResponse> updateMember( @PathVariable Long id,
                                                     @RequestBody MemberUpdateRequestByAdmin request,
                                                     @ModelAttribute PagingRequest pagingRequest) {

        try {
            // 1) Path ID vs. payload ID 검증
            if (!id.equals(request.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND, "잘못된 요청"));
            }

            // 2) 수정 로직 호출
            userService.updateMemberByAdmin(request);

            // 3) 성공 응답
            return ResponseEntity
                    .ok(new ApiResponse(GlobalStatus.OK));

        } catch (FirebaseAuthenticationException fx){
            log.warn("Firebase 서버 예외: {}", fx.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(GlobalStatus.FIREBASE_ERROR));
        } catch (AccessDeniedException ex) {
            log.warn("권한 없음: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(GlobalStatus.ACCESS_DENIED));
        } catch (AuthenticationException ex) {
            log.warn("인증 실패: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(GlobalStatus.AUTHENTICATION_FAIL));
        } catch (EntityNotFoundException ex) {
            log.warn("회원 미존재: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND));
        } catch (JwtException ex) {
            log.warn("JWT 오류: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(GlobalStatus.JWT_VALIDATION_FAIL));
        } catch (Exception ex) {
            log.error("알 수 없는 오류: ", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(GlobalStatus.SERVER_ERROR, "서버 오류 발생"));
        }


    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.UPDATE_EXPERT)
    @PostMapping("/admin/expert/{id:[0-9]+}")
    public ResponseEntity<ApiResponse> updateExpert( @PathVariable Long id,
                                                    @RequestBody @Valid ExpertUpdateRequestByAdmin request,
                                                    @ModelAttribute PagingRequest pagingRequest) {
        try {
            // 1) Path ID vs. payload ID 검증
            if (!id.equals(request.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND, "잘못된 요청: ID 불일치"));
            }

            // 2) 수정 로직 호출
            userService.updateExpertByAdmin(request);

            // 3) 성공 응답
            return ResponseEntity
                    .ok(new ApiResponse(GlobalStatus.OK));

        } catch (AccessDeniedException ex) {
            log.warn("권한 없음: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(GlobalStatus.ACCESS_DENIED));
        } catch (AuthenticationException ex) {
            log.warn("인증 실패: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(GlobalStatus.AUTHENTICATION_FAIL));
        } catch (EntityNotFoundException ex) {
            log.warn("회원 미존재: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND));
        } catch (JwtException ex) {
            log.warn("JWT 오류: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(GlobalStatus.JWT_VALIDATION_FAIL));
        } catch (Exception ex) {
            log.error("알 수 없는 오류: ", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(GlobalStatus.SERVER_ERROR, "서버 오류 발생"));
        }

    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.UPDATE_MANAGER)
    @PostMapping("/admin/manager/{id:[0-9]+}")
    public ResponseEntity<ApiResponse> updateManager(@PathVariable Long id,
                                                     @RequestBody @Valid ManagerUpdateRequestByAdmin request,
                                                     @ModelAttribute PagingRequest pagingRequest) {
        try {
            // 1) Path ID vs. payload ID 검증
            if (!id.equals(request.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND, "잘못된 요청: ID 불일치"));
            }

            // 2) 수정 로직 호출
            userService.updateManagerByAdmin(request);

            // 3) 성공 응답
            return ResponseEntity
                    .ok(new ApiResponse(GlobalStatus.OK));

        } catch (AccessDeniedException ex) {
            log.warn("권한 없음: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(GlobalStatus.ACCESS_DENIED));
        } catch (AuthenticationException ex) {
            log.warn("인증 실패: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(GlobalStatus.AUTHENTICATION_FAIL));
        } catch (EntityNotFoundException ex) {
            log.warn("회원 미존재: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND));
        } catch (JwtException ex) {
            log.warn("JWT 오류: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(GlobalStatus.JWT_VALIDATION_FAIL));
        } catch (Exception ex) {
            log.error("알 수 없는 오류: ", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(GlobalStatus.SERVER_ERROR, "서버 오류 발생"));
        }
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_MEMBER_CHILD_LIST)
    @GetMapping("/admin/member/{id:[0-9]+}/children")
    public String showAdminMemberChildrenPage(@ModelAttribute PagingRequest pagingRequest,
                                            @PathVariable Long id,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        MemberDTO memberDTO;

        try{
            memberDTO = memberService.getMemberById(id);

            log.info(memberDTO.getCreatedAt());
        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/member/all";
        }
        model.addAttribute("hasChildren", !memberDTO.getChildren().isEmpty());
        model.addAttribute("memberDTO", memberDTO);
        if(!memberDTO.getChildren().isEmpty()){
            memberDTO.getChildren().forEach((child)->child.setAge(child.calculateAge()));
        }
        model.addAttribute("children", memberDTO.getChildren());

        return "admin/member/memberChildren";
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_MANAGER_CHILD_LIST)
    @GetMapping("/admin/manager/{id:[0-9]+}/children")
    public String showAdminManagerChildrenPage(@PathVariable Long id,
                                               @ModelAttribute PagingRequest pagingRequest,
                                              RedirectAttributes redirectAttribute,
                                              Model model){
        ManagerDTO managerDTO;
        PagingResultDTO result;

        try{
            managerDTO = managerService.getManagerById(id);
            Pageable pageable = pagingRequest.toPageable();
            result = childService.getChildrenByGroup(managerDTO.getGroupId(), pageable);

        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/manager/all";
        }

        model.addAttribute("hasChildren", !managerDTO.getChildren().isEmpty());
        if(!managerDTO.getChildren().isEmpty()){
            managerDTO.getChildren().forEach((child)->child.setAge(child.calculateAge()));
        }
        model.addAttribute("managerDTO", managerDTO);
        model.addAttribute("children", managerDTO.getChildren());
        model.addAttribute("result",result);

        return "admin/manager/managerChildren";
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.RESTORE_USERS)
    @PostMapping("/admin/users/deleted/restore/{id:[0-9]+}")
    public ResponseEntity<ApiResponse> restoreDeletedUsers(@PathVariable Long id,
                                                           @ModelAttribute PagingRequest pagingRequest,
                                                           @RequestParam(required = false) String type,
                                                           @RequestParam(required = false) String keyword,
                                                           RedirectAttributes redirectAttribute,
                                                           Model model){

        UserDTO userDTO = new UserDTO();
        // 페이징·검색 파라미터 계속 보존
        model.addAttribute("page",      pagingRequest.getPage());
        model.addAttribute("size",      pagingRequest.getSize());
        model.addAttribute("sort",      pagingRequest.getSort());
        model.addAttribute("direction", pagingRequest.getDirection());

        try{
            userService.restoreUsersById(id);

        } catch (UsernameNotFoundException | UserIsNotDeletedException unf){
            model.addAttribute("activeUser", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(GlobalStatus.OK));
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_EXPERT_UNAPPROVE_LIST)
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

        model.addAttribute("hasExpertApplicant", !result.getDtoList().isEmpty());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("direction", pagingRequest.getDirection());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("result", result);

        return "admin/expertApplicants";
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.SEARCH_EXPERT_UNAPPROVE)
    @GetMapping("/admin/expert-applicants/search")
    public ResponseEntity<ApiResponse<?>> searchExpertApplicants(
            @RequestParam String type,
            @RequestParam String keyword,
            @ModelAttribute PagingRequest pagingRequest,
            Model model) {

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ExpertDTO, Expert> result = expertService.searchUnapprovedExpertList(type, keyword, pageable);

        model.addAttribute("sort", pagingRequest.getSort());

        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.APPROVE_EXPERT)
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
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.ACCESS_MANAGER_UNAPPROVE_LIST)
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
        model.addAttribute("hasManagerApplicant", !result.getDtoList().isEmpty());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("direction", pagingRequest.getDirection());
        model.addAttribute("sort", pagingRequest.getSort());
        model.addAttribute("result", result);

        return "admin/managerApplicants"; // 뷰 파일
    }
    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.SEARCH_MANAGER_UNAPPROVE)
    @GetMapping("/admin/manager-applicants/search")
    public ResponseEntity<ApiResponse<?>> searchManagerApplicants(
            @RequestParam String type,
            @RequestParam String keyword,
            @ModelAttribute PagingRequest pagingRequest,
            Model model) {

        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<ManagerDTO, Manager> result = managerService.searchUnapprovedManagerList(type, keyword, pageable);

        model.addAttribute("sort", pagingRequest.getSort());

        if (result.getTotalElements() <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(GlobalStatus.NO_CONTENT));
        }

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, result));
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.APPROVE_MANAGER)
    @PostMapping("/admin/approve-manager")
    public ResponseEntity<ApiResponse> approveManager(@ModelAttribute PagingRequest pagingRequest,
                                                      @RequestBody IdsRequest ids ,
                                                      @RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String keyword,
                                                      RedirectAttributes redirectAttributes,
                                                      Model model) throws FirebaseAuthException {

        // 리스트 순회 -> 승인
        // 1. 파이어 베이스 클레임 변경
        // 2. 데이터베이스 정보 변경
        // 3. 성공 실패에 따라 응답 코드 발생
        try{
            for (Long id : ids.getIds()){
                firebaseService.setRoleToManagerInClaim(id);
                managerService.approveManager(id);
            }
        } catch (UserNotFoundException userNotFoundException){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(AuthStatus.USER_NOT_FOUND));
        } catch (FirebaseAuthenticationException firebaseAuthenticationException){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(GlobalStatus.FIREBASE_ERROR));
        }

        Pageable pageable = pagingRequest.toPageable();

        PagingResultDTO<ManagerDTO, Manager> result = managerService.getUnapprovedManagerList(pageable);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("result",result);


        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(GlobalStatus.OK));
    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.INITIALIZE_ADMIN_PASSWORD)
    @PostMapping("/admin/admin/init/{id:[0-9]+}")
    public ResponseEntity<ApiResponse> initializeAdminPassword(@PathVariable Long id, HttpServletRequest request){

        try{
            String uuid = adminService.getAdminUUIDById(id);
            boolean isSuccess = adminService.initializePassword(id);
            if (isSuccess){
                firebaseService.initFirebasePassword(uuid);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(GlobalStatus.OK));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND));
            }

        } catch (AdminNotFoundException ade){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(GlobalStatus.ENTITY_NOT_FOUND));
        } catch (FirebaseAuthException firebaseAuthException){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(GlobalStatus.FIREBASE_ERROR));
        }





    }

    @AdminActionHistoryAuditLog(category = AdminActionHistoryCategory.SEARCH_MANAGER_UNAPPROVE)
    @PostMapping("/api/admin/manager-applicants")
    public ResponseEntity<ApiResponse<PagingResponse<ManagerDTO>>> getPendingManagerList(@RequestBody PagingRequest request) {
        Pageable pageable = request.toPageable();
        PagingResultDTO<ManagerDTO, Manager> result = managerService.getUnapprovedManagerList(pageable);

        PagingResponse<ManagerDTO> response = PagingResponse.from(result);

        return ResponseEntity.ok(new ApiResponse<>(GlobalStatus.SUCCESS_WITH_DATA, response));
    }

}