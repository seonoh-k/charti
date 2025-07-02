package com.example.demo.survey.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResponse;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.exception.UnauthorizedAccessException;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.survey.dto.RecordDateSummaryDto;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

/**
 * {@code RecordAnswerController}
 *
 * 사용자(보호자)가 자신의 자녀에 대한 기록 문진 답변 이력을 조회할 수 있는 기능을 제공하는 컨트롤러입니다.
 *
 * <p>주요 기능:
 * <ul>
 *     <li>로그인한 사용자의 자녀 목록 조회</li>
 *     <li>특정 자녀의 기록 문진 날짜 목록 조회 (페이징)</li>
 *     <li>특정 자녀의 특정 날짜에 대한 문진 답변 목록 조회</li>
 * </ul>
 */
@Controller
@RequestMapping("/survey/record")
@RequiredArgsConstructor
@Slf4j
public class RecordAnswerController {

    private final ChildService childService;
    private final RecordAnswerService recordAnswerService;
    private final AuthService authService;
    private final UserService userService;

    /**
     * 기록 문진 이력 페이지를 반환합니다. 로그인한 사용자의 자녀 목록을 모델에 포함하여 전달합니다.
     *
     * @param model Thymeleaf 템플릿에서 사용할 모델
     * @return 기록 문진 이력 페이지 뷰 이름
     */
    @GetMapping("/history")
    public String showAnswerHistory(Model model) {
        UserDTO userDTO = authService.getLoginUser();
        Member loginUser = userService.getMemberEntityById(userDTO.getId());
        List<Child> children = childService.getChildrenByMember(loginUser);
        model.addAttribute("children", children);
        return "survey/recordAnswerHistory";
    }

    /**
     * 특정 자녀의 기록 문진 날짜 목록을 페이징하여 조회합니다.
     *
     * @param childId 자녀 ID
     * @param pagingRequest 페이징 요청 정보
     * @return 날짜 목록 페이징 응답
     */
    @GetMapping("/history/{childId}/dates")
    @ResponseBody
    public ResponseEntity<ApiResponse<PagingResponse<RecordDateSummaryDto>>> getRecordDates(
            @PathVariable Long childId,
            PagingRequest pagingRequest) {

        verifyChildOwnership(childId);

        Pageable pageable = pagingRequest.toZeroBasedPageable();
        Page<RecordDateSummaryDto> recordDatesPage = recordAnswerService.getRecordDatesPagedByChild(childId, pageable);
        PagingResultDTO<RecordDateSummaryDto, RecordDateSummaryDto> pagingResultDTO = new PagingResultDTO<>(recordDatesPage);
        PagingResponse<RecordDateSummaryDto> finalResponse = PagingResponse.from(pagingResultDTO);

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, finalResponse));
    }

    /**
     * 특정 자녀의 특정 날짜에 대한 질문-답변 목록을 조회합니다.
     *
     * @param childId 자녀 ID
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @return 해당 날짜의 질문-답변 리스트
     */
    @GetMapping("/history/{childId}/date/{dateStr}")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<RecordAnswerResponse>>> getAnswersForDate(
            @PathVariable Long childId,
            @PathVariable String dateStr) {

        verifyChildOwnership(childId);

        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<RecordAnswerResponse> answers = recordAnswerService.getQuestionsAndAnswersForDate(childId, date);
            return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, answers));
        } catch (Exception e) {
            log.error("날짜 파싱 또는 답변 조회 중 오류 발생: childId={}, dateStr={}", childId, dateStr, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(GlobalStatus.UNKNOWN_ERROR, "답변 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 현재 로그인한 사용자가 해당 자녀의 부모인지 확인합니다.
     *
     * @param childId 자녀 ID
     * @throws UnauthorizedAccessException 권한이 없을 경우 예외 발생
     */
    private void verifyChildOwnership(Long childId) {
        UserDTO loginUser = authService.getLoginUser();
        Child child = childService.findById(childId);
        if (!child.getParent().getId().equals(loginUser.getId())) {
            log.warn("권한 없는 접근 시도: 사용자 ID {}가 자녀 ID {}의 정보에 접근 시도", loginUser.getId(), childId);
            throw new UnauthorizedAccessException("해당 자녀의 정보에 접근할 권한이 없습니다.");
        }
    }
}
