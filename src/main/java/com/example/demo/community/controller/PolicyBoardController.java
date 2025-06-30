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
@RequestMapping("/community/policy")
public class PolicyBoardController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;
    private final UserService userService;

    public PolicyBoardController(
            CommunityBoardService boardService,
            CommentService commentService,
            UserService userService
    ) {
        this.boardService   = boardService;
        this.commentService = commentService;
        this.userService    = userService;
    }

    // 하위 카테고리
    private final List<String> subCategories = Arrays.asList(
            "보육지원", "출산지원", "건강검진", "양육휴가", "재정지원"
    );
    // 연령대 필터 (ALL,VARIOUS 제외)
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(a -> a != AgeGroup.ALL && a != AgeGroup.VARIOUS)
            .collect(Collectors.toList());

    /** 리스트 조회 (필터·검색·정렬·페이징) */
    @GetMapping({"/list", "/list/"})
    public String list(
            Model model,
            @RequestParam(defaultValue = "전체") String subCategory,
            @RequestParam(defaultValue = "ALL") AgeGroup ageGroup,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(name = "sort", defaultValue = "popular") String sortOrder,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<CommunityBoard> result = boardService.getList(
                "policyInformation",
                subCategory, ageGroup, keyword, sortOrder, page
        );
        model.addAttribute("boards",        result.getContent());
        model.addAttribute("pageData",      result);
        model.addAttribute("subCategory",   subCategory);
        model.addAttribute("ageGroup",      ageGroup);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("sort",          sortOrder);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups",     ageGroups);
        return "community/policyList";
    }

    /** 작성 폼 (비로그인 → 로그인폼) */
    @GetMapping("/new")
    public String form(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
        model.addAttribute("board",         new CommunityBoard());
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups",     ageGroups);
        return "community/policyForm";
    }

    /** 생성 (Authentication → Users → usersId) */
    @PostMapping("")
    public String create(
            @ModelAttribute CommunityBoard board,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }

        String principal = auth.getName();
        Users me = null;
        // 1) 이메일 조회
        try {
            me = userService.findByUsernameEntity(principal);
        } catch (UserNotFoundException ignored) { }
        // 2) UUID 조회
        if (me == null) {
            try {
                me = userService.findByUuidEntity(principal);
            } catch (UserNotFoundException ignored) { }
        }
        // 3) 둘 다 실패하면 로그인폼으로
        if (me == null) {
            return "redirect:/loginForm";
        }

        board.setUsersId(me.getId());
        board.setCategory("policyInformation");
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/policy/list";
    }

    /** 상세 (조회수↑, 댓글 페이징, currentUserId 세팅) */
    @GetMapping("/{id:[0-9]+}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int commentPage,
            Model model,
            Authentication auth
    ) {
        // 1) 게시글 + 조회수
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews() + 1);
        boardService.save(board);

        // 2) 댓글 페이징
        Page<Comment> commentsPage = commentService.getCommentsPage(id, commentPage);

        // 3) 작성자 닉네임
        String authorNickname = userService.findNicknameOrDefault(board.getUsersId());

        // 4-1) 댓글 작성자 닉네임 리스트 (
        List<String> commentNicknames = commentsPage.getContent().stream()
                .map(c -> userService.findNicknameOrDefault(c.getUsersId()))
                .collect(Collectors.toList());

        // 4-2) 댓글별 usersId 리스트
        List<Long> commentUserIds = commentsPage.getContent().stream()
                .map(Comment::getUsersId)
                .collect(Collectors.toList());

        // 5) 로그인한 사용자 ID
        Long currentUserId = null;
        if (auth != null && auth.isAuthenticated()) {
            String p = auth.getName();
            Users me = null;
            try { me = userService.findByUsernameEntity(p); }
            catch (UserNotFoundException ignored) { }
            if (me == null) {
                try { me = userService.findByUuidEntity(p); }
                catch (UserNotFoundException ignored) { }
            }
            if (me != null) currentUserId = me.getId();
        }

        model.addAttribute("board",            board);
        model.addAttribute("commentsPage",     commentsPage);
        model.addAttribute("authorNickname",   authorNickname);
        model.addAttribute("commentNicknames", commentNicknames);
        model.addAttribute("commentUserIds",   commentUserIds);
        model.addAttribute("currentUserId",    currentUserId);
        return "community/policyDetail";
    }

    /** 수정 폼 (비로그인→로그인폼) */
    @GetMapping("/{id:[0-9]+}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board",       board);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups",     ageGroups);
        return "community/policyForm";
    }

    /** 수정 처리 (작성자 유지) */
    @PostMapping("/{id:[0-9]+}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board
    ) {
        CommunityBoard old = boardService.findById(id);
        board.setUsersId(old.getUsersId());
        old.setCategory2(board.getCategory2());
        old.setAgeGroup(board.getAgeGroup());
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/community/policy/" + id;
    }

    /** 삭제 */
    @PostMapping("/{id:[0-9]+}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/community/policy/list";
    }

    /** 댓글 등록 (usersId 세팅) */
    @PostMapping("/{id:[0-9]+}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }

        String p = auth.getName();
        Users me = null;
        try { me = userService.findByUsernameEntity(p); }
        catch (UserNotFoundException ignored) { }
        if (me == null) {
            try { me = userService.findByUuidEntity(p); }
            catch (UserNotFoundException ignored) { }
        }
        if (me == null) {
            return "redirect:/loginForm";
        }

        Comment c = new Comment();
        c.setCommunityId(id);
        c.setUsersId(me.getId());
        c.setContent(content);
        commentService.save(c);
        return "redirect:/community/policy/" + id;
    }

    /**
     * 댓글 수정 처리
     */
    @PostMapping("/{boardId:[0-9]+}/comments/{commentId:[0-9]+}")
    public String updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestParam String content,
            Authentication auth
    ) {
        // 1) DB에서 댓글을 가져온 뒤
        Comment c = commentService.findById(commentId);

        // 2) 현재 로그인한 유저 ID와 댓글 작성자 ID를 비교
        Long currentUserId = getCurrentUserId(auth);
        if (currentUserId != null && currentUserId.equals(c.getUsersId())) {
            // 3) 본인이 작성한 댓글이면 내용 업데이트
            c.setContent(content);
            commentService.save(c);
        }

        // 4) 상세 페이지로 리다이렉트
        return "redirect:/community/policy/" + boardId;
    }

    /**
     * 댓글 삭제 처리
     */
    @PostMapping("/{boardId:[0-9]+}/comments/{commentId:[0-9]+}/delete")
    public String deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            Authentication auth
    ) {
        Comment c = commentService.findById(commentId);
        Long currentUserId = getCurrentUserId(auth);
        if (currentUserId != null && currentUserId.equals(c.getUsersId())) {
            commentService.delete(commentId);
        }
        return "redirect:/community/policy/" + boardId;
    }

    /**
     * 인증 객체에서 Users ID를 꺼내는 헬퍼
     */
    private Long getCurrentUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        String principal = auth.getName();
        try {
            return userService.findByUsernameEntity(principal).getId();
        } catch (UserNotFoundException e) {
            return userService.findByUuidEntity(principal).getId();
        }
    }
}
