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
        this.boardService = boardService;
        this.commentService = commentService;
        this.userService = userService;
    }

    // 하위 카테고리
    private final List<String> subCategories = Arrays.asList(
            "보육지원", "출산지원", "건강검진", "양육휴가", "재정지원"
    );
    // AgeGroup enum 필터
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(a -> a != AgeGroup.ALL && a != AgeGroup.VARIOUS)
            .collect(Collectors.toList());

    /** 리스트 */
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

    /** 작성 폼 */
    @GetMapping("/new")
    public String form(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
        model.addAttribute("board", new CommunityBoard());
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/policyForm";
    }

    /** 생성 */
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
        board.setCategory("policyInformation");
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/policy/list";
    }

    /** 상세 */
    @GetMapping("/{id:[0-9]+}")
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

        Comment comment = new Comment();
        comment.setCommunityId(id);
        comment.setUsersId(me.getId());
        comment.setContent(content);
        commentService.save(comment);
        return "redirect:/community/policy/" + id;
    }

    /** 수정 폼 */
    @GetMapping("/{id:[0-9]+}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board", board);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/policyForm";
    }

    /** 수정 처리 */
    @PostMapping("/{id:[0-9]+}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board
    ) {
        CommunityBoard old = boardService.findById(id);
        // 작성자 유지
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
}
