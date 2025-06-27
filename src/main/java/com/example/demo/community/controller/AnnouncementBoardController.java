package com.example.demo.community.controller;

import com.example.demo.community.entity.Comment;
import com.example.demo.community.entity.CommunityBoard;
import com.example.demo.community.service.CommentService;
import com.example.demo.community.service.CommunityBoardService;
import com.example.demo.enums.AgeGroup;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/community/announcement")
public class AnnouncementBoardController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;
    private final UserService userService;

    // 생성자 주입
    public AnnouncementBoardController(
            CommunityBoardService boardService,
            CommentService commentService,
            UserService userService
    ) {
        this.boardService   = boardService;
        this.commentService = commentService;
        this.userService    = userService;
    }

    // 공지사항 하위 카테고리 목록
    private final List<String> subCategories = Arrays.asList(
            "이벤트", "점검안내", "정책변경", "업데이트", "긴급공지"
    );

    // 공지사항 리스트 조회 (하위카테고리 필터, 키워드 검색, 페이징)
    @GetMapping({"", "/list"})
    public String list(
            Model model,
            @RequestParam(defaultValue="전체") String subCategory,
            @RequestParam(defaultValue="") String keyword,
            @RequestParam(defaultValue="popular") String sort,
            @RequestParam(defaultValue="0") int page
    ) {

        Page<CommunityBoard> result = boardService.getList(
                "announcement",
                subCategory,
                AgeGroup.ALL,
                keyword,
                sort,
                page
        );

        model.addAttribute("boards",       result.getContent());
        model.addAttribute("pageData",     result);
        model.addAttribute("subCategory",  subCategory);
        model.addAttribute("keyword",      keyword);
        model.addAttribute("sort",         sort);
        model.addAttribute("subCategories",subCategories);
        return "community/announcementList";
    }

    // 새 공지 작성 폼 이동 (비로그인 시 로그인 페이지로 리다이렉트)
    @GetMapping("/new")
    public String form(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
        // 현재 로그인한 사용자 조회
        String principal = auth.getName();
        Users me;
        try {
            me = userService.findByUsernameEntity(principal);
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(principal);
        }

        model.addAttribute("board",        new CommunityBoard());
        model.addAttribute("subCategories",subCategories);
        model.addAttribute("currentUser",  me);
        return "community/announcementForm";
    }

    // 새 공지 저장 처리
    @PostMapping("")
    public String create(
            @ModelAttribute CommunityBoard board,
            Authentication auth
    ) {
        // 현재 로그인한 사용자 조회
        String principal = auth.getName();
        Users me;
        try {
            me = userService.findByUsernameEntity(principal);
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(principal);
        }

        board.setUsersId(me.getId());          // 작성자 ID 설정
        board.setCategory("announcement");     // 상위 카테고리
        board.setAgeGroup(AgeGroup.ALL);       // 연령대 ALL 고정
        board.setStatus("Y");                  // 상태
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/announcement/list";
    }

    // 공지 상세 조회 + 댓글 페이징
    @GetMapping("/{id:[0-9]+}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue="0") int commentPage,
            Model model
    ) {
        // 조회수 증가
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews()+1);
        boardService.save(board);

        Page<Comment> comments = commentService.getCommentsPage(id, commentPage);
        model.addAttribute("board",        board);
        model.addAttribute("commentsPage", comments);
        return "community/announcementDetail";
    }

    // 댓글 등록 처리
    @PostMapping("/{id:[0-9]+}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication auth
    ) {
        // 로그인 사용자 조회
        String principal = auth.getName();
        Users me;
        try {
            me = userService.findByUsernameEntity(principal);
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(principal);
        }

        Comment c = new Comment();
        c.setCommunityId(id);
        c.setUsersId(me.getId());
        c.setContent(content);
        commentService.save(c);
        return "redirect:/community/announcement/" + id;
    }

    // 공지 수정 폼 이동 (비로그인 시 로그인 페이지로 리다이렉트)
    @GetMapping("/{id:[0-9]+}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
        // 로그인 사용자 조회
        String principal = auth.getName();
        Users me;
        try {
            me = userService.findByUsernameEntity(principal);
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(principal);
        }

        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board",        board);
        model.addAttribute("subCategories",subCategories);
        model.addAttribute("currentUser",  me);
        return "community/announcementForm";
    }

    // 공지 수정 처리
    @PostMapping("/{id:[0-9]+}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board
    ) {
        CommunityBoard old = boardService.findById(id);
        board.setUsersId(old.getUsersId());    // 작성자 유지

        old.setCategory2(board.getCategory2());
        old.setAgeGroup(AgeGroup.ALL);
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/community/announcement/" + id;
    }

    // 공지 삭제 처리
    @PostMapping("/{id:[0-9]+}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/community/announcement/list";
    }
}
