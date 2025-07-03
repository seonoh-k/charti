package com.example.demo.community.controller;

import com.example.demo.community.entity.Comment;
import com.example.demo.community.entity.CommunityBoard;
import com.example.demo.community.service.CommentService;
import com.example.demo.community.service.CommunityBoardService;
import com.example.demo.dto.AdminDTO;
import com.example.demo.enums.AgeGroup;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.AdminService;
import com.example.demo.users.service.AuthService;
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
    private final AdminService adminService;
    private final AuthService authService;

    public AnnouncementBoardController(
            CommunityBoardService boardService,
            CommentService commentService,
            UserService userService,
            AdminService adminService,
            AuthService authService
    ) {
        this.boardService   = boardService;
        this.commentService = commentService;
        this.adminService    = adminService;
        this.authService  = authService;
        this.userService = userService;
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

        List<String> authorNames = result.getContent().stream()
                .map(b -> adminService.getAdminById(b.getAdminId()).getName())
                .collect(Collectors.toList());

        model.addAttribute("authorNames",    authorNames);
        model.addAttribute("boards",        result.getContent());
        model.addAttribute("pageData",      result);
        model.addAttribute("subCategory",   subCategory);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("sort",          sort);
        model.addAttribute("subCategories", subCategories);
        return "community/announcementList";
    }

    @GetMapping("/new")
    public String form(Model model) {
        AdminDTO admin = authService.getLoginAdmin();

        model.addAttribute("board",        new CommunityBoard());
        model.addAttribute("subCategories",subCategories);
        model.addAttribute("currentUser",  admin);
        return "admin/admin/announcementForm";
    }

    @PostMapping("")
    public String create(
            @ModelAttribute CommunityBoard board
    ) {
        AdminDTO admin = authService.getLoginAdmin();

        board.setAdminId(admin.getId());
        board.setCategory("announcement");
        board.setAgeGroup(AgeGroup.ALL);
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/admin/announcement";
    }

    @GetMapping("/{id:[0-9]+}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int commentPage,
            Model model,
            Authentication auth
    ) {
        // 게시글 + 조회수
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews()+1);
        boardService.save(board);

        // 글 작성자 닉네임
        String authorName = adminService.getAdminById(board.getAdminId()).getName();

        // 댓글 페이징
        Page<Comment> commentsPage = commentService.getCommentsPage(id, commentPage);

        // 댓글 작성자 닉네임 리스트
        List<String> commentNicknames = commentsPage.getContent().stream()
                .map(c -> userService.findNicknameOrDefault(c.getUsersId()))
                .collect(Collectors.toList());

        // 댓글별 usersId 리스트
        List<Long> commentUserIds = commentsPage.getContent().stream()
                .map(Comment::getUsersId)
                .collect(Collectors.toList());

        // 로그인한 사용자 ID
        Long currentUserId = getCurrentUserId(auth);

        model.addAttribute("board",             board);
        model.addAttribute("authorName",       authorName);
        model.addAttribute("commentsPage",      commentsPage);
        model.addAttribute("commentNicknames",  commentNicknames);
        model.addAttribute("commentUserIds",    commentUserIds);
        model.addAttribute("currentUserId",     currentUserId);
        return "community/announcementDetail";
    }

    @GetMapping("/{id:[0-9]+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        AdminDTO admin = authService.getLoginAdmin();

        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board",        board);
        model.addAttribute("subCategories",subCategories);
        model.addAttribute("currentUser",  admin);
        return "admin/admin/announcementForm";
    }

    @PostMapping("/{id:[0-9]+}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board
    ) {
        CommunityBoard old = boardService.findById(id);
        board.setAdminId(old.getAdminId());
        old.setCategory2(board.getCategory2());
        old.setAgeGroup(AgeGroup.ALL);
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/admin/announcement/" + id;
    }

    @PostMapping("/{id:[0-9]+}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/admin/announcement";
    }

    // 댓글 작성 처리
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

    // 댓글 수정
    @PostMapping("/{boardId:[0-9]+}/comments/{commentId:[0-9]+}")
    public String updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestParam String content,
            Authentication auth
    ) {
        Comment c = commentService.findById(commentId);
        Long me = getCurrentUserId(auth);
        if (me != null && me.equals(c.getUsersId())) {
            c.setContent(content);
            commentService.save(c);
        }
        return "redirect:/community/announcement/" + boardId;
    }

    // 댓글 삭제
    @PostMapping("/{boardId:[0-9]+}/comments/{commentId:[0-9]+}/delete")
    public String deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            Authentication auth
    ) {
        Comment c = commentService.findById(commentId);
        Long me = getCurrentUserId(auth);
        if (me != null && me.equals(c.getUsersId())) {
            commentService.delete(commentId);
        }
        return "redirect:/community/announcement/" + boardId;
    }

    // 인증 객체 → Users ID 꺼내는 헬퍼
    private Long getCurrentUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        String p = auth.getName();
        Users me = null;
        try { me = userService.findByUsernameEntity(p); }
        catch (UserNotFoundException ignored) { }
        if (me == null) {
            try { me = userService.findByUuidEntity(p); }
            catch (UserNotFoundException ignored) { }
        }
        return me != null ? me.getId() : null;
    }

}
