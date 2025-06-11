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
@RequestMapping("/community")
public class CommunityBoardController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;
    public CommunityBoardController(
            CommunityBoardService boardService,
            CommentService commentService) {
        this.boardService = boardService;
        this.commentService = commentService;
    }

    private final List<String> subCategories = Arrays.asList(
            "수유", "수면", "위생", "발달", "건강"
    );
    private final List<String> ageGroups = Arrays.asList(
            "0~1세", "2~3세", "4~5세", "상관없음"
    );

    /**
     * 게시글 리스트 페이지를 보여줍니다.
     * - 검색, 필터, 정렬, 페이징 파라미터를 받아 Service로 전달 후 결과를 뷰에 바인딩
     */
    @GetMapping({"/granulation/list", "/granulation/list/"})
    public String list(
            Model model,
            @RequestParam(defaultValue = "전체") String subCategory,
            @RequestParam(defaultValue = "상관없음") String ageGroup,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(name = "sort", defaultValue = "popular") String sortOrder,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<CommunityBoard> result = boardService.getList(
                subCategory, ageGroup, keyword, sortOrder, page);
        model.addAttribute("boards", result.getContent());
        model.addAttribute("pageData", result);
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("ageGroup", ageGroup);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sortOrder);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/granulationList";
    }

    /**
     * 새 게시글 작성 폼 화면으로 이동합니다.
     */
    @GetMapping("/granulation/new")
    public String form(Model model) {
        model.addAttribute("board", new CommunityBoard());
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/granulationForm";
    }

    /**
     * 새 게시글을 생성하고 리스트 페이지로 리다이렉트합니다.
     */
    @PostMapping("/granulation")
    public String create(@ModelAttribute CommunityBoard board) {
        board.setCategory("parentingInformation");
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/granulation/list";
    }

    /**
     * 단일 게시글 상세 페이지를 보여줍니다.
     * 댓글 페이징을 함께 처리합니다.
     */
    @GetMapping("/granulation/{id:[0-9]+}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int commentPage,
            Model model
    ) {
        // 게시글
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews() + 1);
        boardService.save(board);

        // 댓글 페이징
        Page<Comment> commentsPage = commentService.getCommentsPage(id, commentPage);

        model.addAttribute("board", board);
        model.addAttribute("commentsPage", commentsPage);
        return "community/granulationDetail";
    }

    /**
     * 댓글을 저장하고 해당 게시글 상세 페이지로 리다이렉트합니다.
     */
    @PostMapping("/granulation/{id:[0-9]+}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content) {
        Comment comment = new Comment();
        comment.setCommunityId(id);
        comment.setContent(content);
        commentService.save(comment);
        return "redirect:/community/granulation/" + id;
    }

    /**
     * 게시글 수정 폼을 보여줍니다.
     */
    @GetMapping("/granulation/{id:[0-9]+}/edit") public String editForm(@PathVariable Long id, Model model) {
        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board", board);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/form";
    }

    /**
     * 게시글 수정을 처리하고 상세 페이지로 리다이렉트합니다.
     */
    @PostMapping("/granulation/{id:[0-9]+}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board) {
        CommunityBoard old = boardService.findById(id);
        old.setCategory2(board.getCategory2());
        old.setAgeGroup(board.getAgeGroup());
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/community/granulation/" + id;
    }

    /**
     * 게시글 삭제를 처리하고 리스트 페이지로 리다이렉트합니다.
     */
    @PostMapping("/granulation/{id:[0-9]+}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/community/granulation/list";
    }


}