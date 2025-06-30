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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/community/announcement")
public class AnnouncementBoardController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;
    private final UserService userService;

    public AnnouncementBoardController(
            CommunityBoardService boardService,
            CommentService commentService,
            UserService userService
    ) {
        this.boardService   = boardService;
        this.commentService = commentService;
        this.userService    = userService;
    }

    private final List<String> subCategories = Arrays.asList(
            "이벤트", "점검안내", "정책변경", "업데이트", "긴급공지"
    );

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
        model.addAttribute("boards",        result.getContent());
        model.addAttribute("pageData",      result);
        model.addAttribute("subCategory",   subCategory);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("sort",          sort);
        model.addAttribute("subCategories", subCategories);
        return "community/announcementList";
    }

    @GetMapping("/new")
    public String form(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
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

    @PostMapping("")
    public String create(
            @ModelAttribute CommunityBoard board,
            Authentication auth
    ) {
        String principal = auth.getName();
        Users me;
        try {
            me = userService.findByUsernameEntity(principal);
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(principal);
        }
        board.setUsersId(me.getId());
        board.setCategory("announcement");
        board.setAgeGroup(AgeGroup.ALL);
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/announcement/list";
    }

    @GetMapping("/{id:[0-9]+}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue="0") int commentPage,
            Model model,
            Authentication auth
    ) {
        // 1) 조회수 증가
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews()+1);
        boardService.save(board);

        // 2) 댓글 페이징
        Page<Comment> commentsPage = commentService.getCommentsPage(id, commentPage);

        // 3) 글 작성자 닉네임
        String authorNickname = userService.findNicknameOrDefault(board.getUsersId());

        // 4) 댓글 작성자 닉네임 리스트
        List<String> commentNicknames = commentsPage.getContent().stream()
                .map(c -> userService.findNicknameOrDefault(c.getUsersId()))
                .collect(Collectors.toList());

        // 5) 로그인한 사용자 ID (버튼 노출 제어용)
        Long currentUserId = null;
        if (auth != null && auth.isAuthenticated()) {
            try {
                currentUserId = userService.findByUsernameEntity(auth.getName()).getId();
            } catch (UserNotFoundException e) {
                currentUserId = userService.findByUuidEntity(auth.getName()).getId();
            }
        }

        model.addAttribute("board",            board);
        model.addAttribute("commentsPage",     commentsPage);
        model.addAttribute("authorNickname",   authorNickname);
        model.addAttribute("commentNicknames", commentNicknames);
        model.addAttribute("currentUserId",    currentUserId);
        return "community/announcementDetail";
    }

    @PostMapping("/{id:[0-9]+}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication auth
    ) {
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

    @GetMapping("/{id:[0-9]+}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
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

    @PostMapping("/{id:[0-9]+}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board
    ) {
        CommunityBoard old = boardService.findById(id);
        board.setUsersId(old.getUsersId());
        old.setCategory2(board.getCategory2());
        old.setAgeGroup(AgeGroup.ALL);
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/community/announcement/" + id;
    }

    @PostMapping("/{id:[0-9]+}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/community/announcement/list";
    }
}
