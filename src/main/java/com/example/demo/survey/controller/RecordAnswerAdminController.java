package com.example.demo.survey.controller;

import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResponse;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.users.dto.ChildHistorySummaryDto;
import com.example.demo.survey.dto.RecordDateSummaryDto;
import com.example.demo.survey.dto.UpdateAnswerRequestDto;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.UserService;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 관리자용 기록문진 이력 컨트롤러
 * - 특정 보호자의 자녀 목록 및 자녀별 날짜, 답변 목록을 조회하거나 수정/삭제하는 기능을 제공합니다.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/surveys/record")
public class RecordAnswerAdminController {

    private final UserService userService;
    private final RecordAnswerService recordAnswerService;

    /**
     * 관리자 페이지 진입 시 기록문진 이력 조회 화면을 반환합니다.
     * 보호자 ID가 주어질 경우, 해당 보호자의 자녀 및 자녀별 문진 이력 요약을 함께 로드합니다.
     */
    @GetMapping("/history")
    public String viewAnswerHistory(@RequestParam(value = "memberId", required = false) Long memberId,
                                    Model model) {
        if (memberId != null) {
            try {
                Member member = userService.getMemberEntityById(memberId);
                model.addAttribute("member", member);
                List<ChildHistorySummaryDto> childrenSummaries = recordAnswerService.getChildrenWithHistorySummary(memberId);
                model.addAttribute("childrenSummaries", childrenSummaries);
                model.addAttribute("hasNoChildren", childrenSummaries.isEmpty());
            } catch (UserNotFoundException e) {
                log.warn("요청된 회원을 찾을 수 없습니다 (ID: {}): {}", memberId, e.getMessage());
                model.addAttribute("errorMessage", e.getMessage());
                return "admin/surveys/recordView";
            } catch (Exception e) {
                log.error("관리자 문진 이력 페이지 로드 중 오류 발생 (memberId: {})", memberId, e);
                model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다. 다시 시도해주세요.");
                return "errorPage";
            }
        }
        return "admin/surveys/recordView";
    }

    /**
     * 특정 자녀가 작성한 문진 날짜 목록을 페이징하여 반환합니다.
     *
     * @param childId 자녀 ID
     * @param pageRequest 페이징 요청 객체
     * @return 날짜 요약 페이징 응답
     */
    @GetMapping("/api/child/{childId}/dates")
    @ResponseBody
    public ResponseEntity<ApiResponse<PagingResponse<RecordDateSummaryDto>>> getChildRecordDates(
            @PathVariable("childId") Long childId,
            PagingRequest pageRequest) {

        Pageable pageable = pageRequest.toZeroBasedPageable();
        Page<RecordDateSummaryDto> recordDatesPage = recordAnswerService.getRecordDatesPagedByChild(childId, pageable);
        PagingResultDTO<RecordDateSummaryDto, RecordDateSummaryDto> pagingResultDTO = new PagingResultDTO<>(recordDatesPage);
        PagingResponse<RecordDateSummaryDto> finalResponse = PagingResponse.from(pagingResultDTO);

        return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, finalResponse));
    }

    /**
     * 특정 자녀의 특정 날짜 문진 질문-답변 목록을 조회합니다.
     *
     * @param childId 자녀 ID
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @return 질문-답변 응답 리스트
     */
    @GetMapping("/api/child/{childId}/date/{dateStr}/answers")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<RecordAnswerResponse>>> getAnswersForDate(@PathVariable("childId") Long childId,
                                                                                     @PathVariable("dateStr") String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<RecordAnswerResponse> answers = recordAnswerService.getQuestionsAndAnswersForDate(childId, date);
            return ResponseEntity.ok(ApiResponse.success(GlobalStatus.SUCCESS_WITH_DATA, answers));
        } catch (DateTimeParseException e) {
            log.warn("잘못된 날짜 형식으로 요청: {}", dateStr);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(GlobalStatus.VALIDATION_FAIL, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"));
        }
    }

    /**
     * 특정 자녀의 특정 날짜 문진 답변들을 일괄 수정합니다.
     *
     * @param childId 자녀 ID
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @param updatedAnswers 수정 요청 DTO 리스트
     * @return 수정 결과 메시지 응답
     */
    @PutMapping("/api/child/{childId}/date/{dateStr}/answers")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> updateRecordAnswers(@PathVariable("childId") Long childId,
                                                                 @PathVariable("dateStr") String dateStr,
                                                                 @RequestBody List<UpdateAnswerRequestDto> updatedAnswers) {
        try {
            LocalDate recordDate = LocalDate.parse(dateStr);
            recordAnswerService.updateRecordAnswers(childId, recordDate, updatedAnswers);
            return ResponseEntity.ok(ApiResponse.success(GlobalStatus.NO_CONTENT, "문진 답변이 성공적으로 수정되었습니다."));
        } catch (DateTimeParseException e) {
            log.warn("잘못된 날짜 형식으로 요청: {}", dateStr);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(GlobalStatus.VALIDATION_FAIL, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"));
        }
    }

    /**
     * 특정 자녀의 특정 날짜 문진 답변들을 삭제 처리합니다.
     *
     * @param childId 자녀 ID
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @return 삭제 결과 메시지 응답
     */
    @DeleteMapping("/api/child/{childId}/date/{dateStr}/answers")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> deleteRecordAnswers(@PathVariable("childId") Long childId,
                                                                 @PathVariable("dateStr") String dateStr) {
        try {
            LocalDate recordDate = LocalDate.parse(dateStr);
            recordAnswerService.deleteRecordAnswers(childId, recordDate);
            return ResponseEntity.ok(ApiResponse.success(GlobalStatus.NO_CONTENT, "문진 기록이 성공적으로 삭제되었습니다."));
        } catch (DateTimeParseException e) {
            log.warn("잘못된 날짜 형식으로 요청: {}", dateStr);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(GlobalStatus.VALIDATION_FAIL, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"));
        }
    }
}
