package com.example.demo.survey.controller;

import com.example.demo.dto.PagingDTO;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.users.dto.ChildHistorySummaryDto;
import com.example.demo.survey.dto.RecordDateSummaryDto;
import com.example.demo.survey.dto.UpdateAnswerRequestDto;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 관리자용 기록문진 이력 컨트롤러
 * - 보호자 ID를 기준으로 자녀 -> 날짜 -> 답변 목록을 조회 및 관리
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/surveys/record")
public class RecordAnswerAdminController {

    private final UserService userService;
    private final RecordAnswerService recordAnswerService;

    /**
     * 기록문진 이력 페이지 진입
     *
     * @param memberId 보호자 ID (선택)
     * @param model Thymeleaf 모델 객체
     * @return 기록문진 이력 뷰 페이지
     */
    @GetMapping("/history")
    public String viewAnswerHistory(@RequestParam(value = "memberId", required = false) Long memberId,
                                    Model model) {
        if (memberId != null) {
            try {
                Member member = userService.getMemberEntityById(memberId); // UserNotFoundException 발생 가능
                model.addAttribute("member", member);
                List<ChildHistorySummaryDto> childrenSummaries = recordAnswerService.getChildrenWithHistorySummary(memberId);
                model.addAttribute("childrenSummaries", childrenSummaries);
                model.addAttribute("hasNoChildren", childrenSummaries.isEmpty());
            } catch (UserNotFoundException e) { // ✨ UserNotFoundException을 명시적으로 처리
                log.warn("요청된 회원을 찾을 수 없습니다 (ID: {}): {}", memberId, e.getMessage());
                model.addAttribute("member", null); // 회원 정보를 찾지 못했음을 모델에 반영
                model.addAttribute("childrenSummaries", Collections.emptyList());
                model.addAttribute("hasNoChildren", true);
                model.addAttribute("errorMessage", e.getMessage()); // 에러 메시지를 뷰로 전달
                return "admin/surveys/recordView"; // 동일 뷰를 반환하거나 에러 페이지로 리다이렉트 (선택)
            } catch (Exception e) { // ✨ 기타 예상치 못한 오류에 대한 일반적인 처리
                log.error("관리자 문진 이력 페이지 로드 중 오류 발생 (memberId: {})", memberId, e);
                model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다. 다시 시도해주세요.");
                return "errorPage"; // 또는 다른 공통 에러 뷰
            }
        } else {
            model.addAttribute("member", null);
            model.addAttribute("childrenSummaries", Collections.emptyList());
            model.addAttribute("hasNoChildren", true);
        }
        return "admin/surveys/recordView";
    }

    /**
     * 특정 자녀의 문진 날짜 목록 조회 (Ajax)
     *
     * @param childId 자녀 ID
     * @param page 페이징 DTO
     * @return 날짜 요약 페이지 정보
     */
    @GetMapping("/api/child/{childId}/dates")
    @ResponseBody
    public ResponseEntity<Page<RecordDateSummaryDto>> getChildRecordDates(@PathVariable("childId") Long childId,
                                                                          PagingDTO<RecordDateSummaryDto> page) {
        if (page.getPage() == null || page.getPage() <= 0) {
            page.setPage(1);
        }

        Pageable pageable = page.toPageable();
        if (page.getSort() == null || page.getSort().isBlank()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<RecordDateSummaryDto> recordDatesPage = recordAnswerService.getRecordDatesPagedByChild(childId, pageable);
        return ResponseEntity.ok(recordDatesPage);
    }

    /**
     * 특정 날짜의 문진 답변 조회 (Ajax)
     *
     * @param childId 자녀 ID
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @return 질문-답변 응답 리스트
     */
    @GetMapping("/api/child/{childId}/date/{dateStr}/answers")
    @ResponseBody
    public ResponseEntity<List<RecordAnswerResponse>> getAnswersForDate(@PathVariable("childId") Long childId,
                                                                        @PathVariable("dateStr") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        List<RecordAnswerResponse> answers = recordAnswerService.getQuestionsAndAnswersForDate(childId, date);
        return ResponseEntity.ok(answers);
    }

    /**
     * 특정 날짜의 문진 답변 수정 처리 (Ajax)
     *
     * @param childId 자녀 ID
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @param updatedAnswers 수정할 질문-답변 목록
     * @return 처리 결과 메시지
     */
    public ResponseEntity<?> updateRecordAnswers(@PathVariable("childId") Long childId,
                                                 @PathVariable("dateStr") String dateStr,
                                                 @RequestBody List<UpdateAnswerRequestDto> updatedAnswers) {
        LocalDate recordDate = LocalDate.parse(dateStr);
        recordAnswerService.updateRecordAnswers(childId, recordDate, updatedAnswers);
        return ResponseEntity.ok(Map.of("message", "문진 답변이 성공적으로 수정되었습니다."));
    }

    /**
     * 특정 날짜의 문진 답변 일괄 삭제 (Ajax)
     *
     * @param childId 자녀 ID
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @return 처리 결과 메시지
     */
    @DeleteMapping("/api/child/{childId}/date/{dateStr}/answers")
    @ResponseBody
    public ResponseEntity<?> deleteRecordAnswers(@PathVariable("childId") Long childId,
                                                 @PathVariable("dateStr") String dateStr) {
        LocalDate recordDate = LocalDate.parse(dateStr);
        recordAnswerService.deleteRecordAnswers(childId, recordDate);
        return ResponseEntity.ok(Map.of("message", "문진 기록이 성공적으로 삭제되었습니다."));
    }
}
