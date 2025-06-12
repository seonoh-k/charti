package com.example.demo.community.controller;

import com.example.demo.community.entity.Comment;
import com.example.demo.community.entity.CommunityBoard;
import com.example.demo.community.service.CommentService;
import com.example.demo.community.service.CommunityBoardService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/community/allPosts")
public class AllPostsBoardController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;

    public AllPostsBoardController(CommunityBoardService boardService,
                                   CommentService commentService) {
        this.boardService   = boardService;
        this.commentService = commentService;
    }

    /**
     * 전체글 리스트 페이지
     * - 제목 키워드 검색
     * - 인기순(조회수), 최신순 정렬
     * - 페이징
     */
    @GetMapping({"/list", "/list/"})
    public String list(
            Model model,
            @RequestParam(defaultValue = "전체") String mainCategory,
            @RequestParam(defaultValue = "전체") String subCategory,
            @RequestParam(defaultValue = "상관없음") String ageGroup,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(name = "sort", defaultValue = "popular") String sortOrder,
            @RequestParam(defaultValue = "0") int page
    ) {

        Page<CommunityBoard> result = boardService.getList(
                mainCategory, subCategory, ageGroup, keyword, sortOrder, page);

        model.addAttribute("boards", result.getContent());
        model.addAttribute("pageData", result);
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("ageGroup", ageGroup);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sortOrder);
        return "community/allPostsList";
    }

    /**
     * 단일 게시글 상세 페이지를 보여줍니다.
     * 댓글 페이징을 함께 처리합니다.
     */
    @GetMapping("/{id:[0-9]+}")
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
        return "community/allPostsDetail";
    }

    /**
     * 댓글을 저장하고 해당 게시글 상세 페이지로 리다이렉트합니다.
     */
    @PostMapping("/{id:[0-9]+}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content) {
        Comment comment = new Comment();
        comment.setCommunityId(id);
        comment.setContent(content);
        commentService.save(comment);
        return "redirect:/community/allPosts/" + id;
    }
}
