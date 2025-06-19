package com.example.demo.survey.controller;

import com.example.demo.dto.PagingDTO;
import com.example.demo.users.dto.ChildHistorySummaryDto;
import com.example.demo.survey.dto.RecordDateSummaryDto;
import com.example.demo.survey.dto.QuestionAnswerPairDto;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

/**
 * 🔍 [관리자용 기록문진 이력 컨트롤러]
 * - 보호자 ID를 기준으로 자녀 → 날짜 → 문진 답변 트리 구조로 전체 조회 (Ajax 동적 로딩)
 * - 날짜 목록은 서버 측에서 페이징 처리
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/surveys/record") // 공통 경로
public class RecordAnswerAdminController {

    private final UserService userService;
    private final RecordAnswerService recordAnswerService;

    /**
     * 기록 문진 이력 메인 조회 페이지 (초기 로드)
     * - 부모 검색 폼과 선택된 부모의 자녀 목록을 표시
     *
     * @param memberId 선택된 보호자 ID (선택 사항)
     * @param model Thymeleaf 모델
     * @return HTML 템플릿 경로
     */
    @GetMapping("/history")
    public String viewAnswerHistory(
            @RequestParam(value = "memberId", required = false) Long memberId,
            Model model
    ) {
        if (memberId != null) {
            Member member = userService.getMemberEntityById(memberId);
            model.addAttribute("member", member);

            List<ChildHistorySummaryDto> childrenSummaries = recordAnswerService.getChildrenWithHistorySummary(memberId);
            model.addAttribute("childrenSummaries", childrenSummaries);
        }

        return "admin/surveys/recordView";
    }

    /**
     * [Ajax] 특정 자녀의 문진 기록 날짜 목록을 페이징하여 가져옵니다.
     * @param childId 자녀 ID
     * @param page PagingDTO (페이지, 사이즈, 정렬)
     * @return RecordDateSummaryDto의 Page 객체 (JSON 응답)
     */
    @GetMapping("/api/child/{childId}/dates")
    @ResponseBody
    public ResponseEntity<Page<RecordDateSummaryDto>> getChildRecordDates(
            @PathVariable("childId") Long childId,
            PagingDTO<RecordDateSummaryDto> page
    ) {
        log.info("🗓️ [관리자] 자녀 ID {} 의 문진 날짜 목록 요청 - 페이지: {}, 사이즈: {}", childId, page.getPage(), page.getSize());

        if (page.getPage() != null) {
            page.setPage(page.getPage() + 1);
        } else {
            page.setPage(1);
        }

        Pageable pageable = page.toPageable();

        if(page.getSort() == null || page.getSort().isBlank()){
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<RecordDateSummaryDto> recordDatesPage = recordAnswerService.getRecordDatesPagedByChild(childId, pageable);
        return ResponseEntity.ok(recordDatesPage);
    }

    /**
     * [Ajax] 특정 자녀의 특정 날짜에 대한 모든 질문-답변 쌍을 가져옵니다.
     * @param childId 자녀 ID
     * @param dateStr 날짜 문자열 (YYYY-MM-DD)
     * @return QuestionAnswerPairDto 리스트 (JSON 응답)
     */
    @GetMapping("/api/child/{childId}/date/{dateStr}/answers")
    @ResponseBody
    public ResponseEntity<List<QuestionAnswerPairDto>> getAnswersForDate(
            @PathVariable("childId") Long childId,
            @PathVariable("dateStr") String dateStr
    ) {
        log.info("📝 [관리자] 자녀 ID {} 의 {} 날짜 답변 상세 요청", childId, dateStr);
        LocalDate date = LocalDate.parse(dateStr);
        List<QuestionAnswerPairDto> answers = recordAnswerService.getQuestionsAndAnswersForDate(childId, date);
        return ResponseEntity.ok(answers);
    }
}
