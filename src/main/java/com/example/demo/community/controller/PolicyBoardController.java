package com.example.demo.community.controller;

import com.example.demo.community.entity.Comment;
import com.example.demo.community.entity.CommunityBoard;
import com.example.demo.community.service.CommentService;
import com.example.demo.community.service.CommunityBoardService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/community/policy")
public class PolicyBoardController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;
    public PolicyBoardController(
            CommunityBoardService boardService,
            CommentService commentService) {
        this.boardService = boardService;
        this.commentService = commentService;
    }

    // 정책정보 관련 하위 카테고리 5개
    private final List<String> subCategories = Arrays.asList(
            "보육지원", "출산지원", "건강검진", "양육휴가", "재정지원"
    );

    // 연령대 필터(기존과 동일)
    private final List<String> ageGroups = Arrays.asList(
            "0~1세", "2~3세", "4~5세", "상관없음"
    );

    /**
     * 정책정보 게시판 리스트 조회
     */
    @GetMapping({"/list", "/list/"})
    public String list(
            Model model,
            @RequestParam(defaultValue = "전체") String subCategory,
            @RequestParam(defaultValue = "상관없음") String ageGroup,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(name = "sort", defaultValue = "popular") String sortOrder,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<CommunityBoard> result = boardService.getList(
            "policyInformation",
            subCategory, ageGroup, keyword, sortOrder, page);

        model.addAttribute("boards", result.getContent());
        model.addAttribute("pageData", result);
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("ageGroup", ageGroup);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sortOrder);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/policyList";
    }

    /**
     * 정책정보 작성 폼 이동
     */
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("board", new CommunityBoard());
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/policyForm";
    }

    /**
     * 정책정보 게시글 생성
     */
    @PostMapping("/new")
    public String create(@ModelAttribute CommunityBoard board) {
        board.setCategory("policyInformation");
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/policy/list";
    }

    /**
     * 상세보기 (조회수 증가 + 댓글 페이징)
     */
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int commentPage,
            Model model
    ) {
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews() + 1);
        boardService.save(board);

        Page<Comment> commentsPage = commentService.getCommentsPage(id, commentPage);
        model.addAttribute("board", board);
        model.addAttribute("commentsPage", commentsPage);
        return "community/policyDetail";
    }

    /** 댓글 등록 */
    @PostMapping("/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content) {
        Comment comment = new Comment();
        comment.setCommunityId(id);
        comment.setContent(content);
        commentService.save(comment);
        return "redirect:/community/policy/" + id;
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board", board);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/policyForm";
    }

    /** 수정 처리 */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board) {
        CommunityBoard old = boardService.findById(id);
        old.setCategory2(board.getCategory2());
        old.setAgeGroup(board.getAgeGroup());
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/community/policy/" + id;
    }

    /** 삭제 처리 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/community/policy/list";
    }
}