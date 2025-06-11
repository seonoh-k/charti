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
import java.util.List;
import java.util.Arrays;

@Controller
@RequestMapping("/community/announcement")
public class AnnouncementBoardController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;

    public AnnouncementBoardController(CommunityBoardService boardService,
                                       CommentService commentService) {
        this.boardService   = boardService;
        this.commentService = commentService;
    }

    // 하위 카테고리 (공지사항 성격별 5개 예시)
    private final List<String> subCategories = Arrays.asList(
            "이벤트", "점검안내", "정책변경", "업데이트", "긴급공지"
    );

    /**
     * 공지사항 리스트 페이지
     * - 하위카테고리 / 연령대 필터
     * - 제목 키워드 검색
     * - 조회수 순 정렬 (항상 views desc)
     * - 페이징
     */
    @GetMapping({"", "/list"})
    public String list(
            Model model,
            @RequestParam(defaultValue="전체")     String subCategory,
            @RequestParam(defaultValue="상관없음") String ageGroup,
            @RequestParam(defaultValue="")        String keyword,
            @RequestParam(defaultValue="0")       int page
    ) {
        // mainCategory="announcement", sortOrder은 항상 "popular"
        Page<CommunityBoard> result = boardService.getList(
                "announcement",
                subCategory, ageGroup, keyword,
                /*sortOrder=*/"popular",
                page
        );

        model.addAttribute("boards",       result.getContent());
        model.addAttribute("pageData",     result);
        model.addAttribute("subCategory",  subCategory);
        model.addAttribute("ageGroup",     ageGroup);
        model.addAttribute("keyword",      keyword);
        model.addAttribute("subCategories",subCategories);
        return "community/announcementList";
    }

    /**
     * 공지 작성 폼
     */
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("board", new CommunityBoard());
        model.addAttribute("subCategories", subCategories);
        return "community/announcementForm";
    }

    /**
     * 공지 저장 후 리스트로 리다이렉트
     */
    @PostMapping("/new")
    public String create(@ModelAttribute CommunityBoard board) {
        board.setCategory("announcement");
        board.setAgeGroup("N/A");
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/announcement/list";
    }

    /**
     * 공지 상세 (조회수 증가 + 댓글 페이징)
     */
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue="0") int commentPage,
            Model model
    ) {
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews()+1);
        boardService.save(board);

        Page<Comment> comments = commentService.getCommentsPage(id, commentPage);
        model.addAttribute("board",        board);
        model.addAttribute("commentsPage", comments);
        return "community/announcementDetail";
    }

    /**
     * 댓글 등록
     */
    @PostMapping("/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content
    ) {
        Comment c = new Comment();
        c.setCommunityId(id);
        c.setContent(content);
        commentService.save(c);
        return "redirect:/community/announcement/" + id;
    }

    /**
     * 공지 수정 폼
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board",         board);
        model.addAttribute("subCategories", subCategories);
        return "community/announcementForm";
    }

    /**
     * 공지 수정 처리 후 상세로 리다이렉트
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board
    ) {
        CommunityBoard old = boardService.findById(id);
        old.setCategory2(board.getCategory2());
        old.setAgeGroup("N/A");
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/community/announcement/" + id;
    }

    /**
     * 공지 삭제
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/community/announcement/list";
    }
}

