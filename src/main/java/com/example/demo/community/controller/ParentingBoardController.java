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
@RequestMapping("/community/parenting")
public class ParentingBoardController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;
    private final UserService userService;

    public ParentingBoardController(
            CommunityBoardService boardService,
            CommentService commentService,
            UserService userService
    ) {
        this.boardService = boardService;
        this.commentService = commentService;
        this.userService = userService;
    }

    private final List<String> subCategories = Arrays.asList(
            "수유", "수면", "위생", "발달", "건강"
    );
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(age -> age != AgeGroup.ALL && age != AgeGroup.VARIOUS)
            .collect(Collectors.toList());

    /** 게시글 리스트 페이지를 보여줍니다. */
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
                "parentingInformation",
                subCategory,
                ageGroup,
                keyword,
                sortOrder,
                page);

        model.addAttribute("boards", result.getContent());
        model.addAttribute("pageData", result);
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("ageGroup", ageGroup);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sortOrder);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/parentingList";
    }

    /** 새 게시글 작성 폼 화면으로 이동합니다. */
    @GetMapping("/new")
    public String form(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/loginForm";
        }
        model.addAttribute("board", new CommunityBoard());
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/parentingForm";
    }

    /** 새 게시글을 생성하고 리스트 페이지로 리다이렉트합니다. */
    @PostMapping("")
    public String create(
            @ModelAttribute CommunityBoard board,
            Authentication authentication
    ) {
        // 1) principal name 으로 Users 조회
        String username = authentication.getName();
        Users currentUser;
        try {
            currentUser = userService.findByUsernameEntity(username);
        } catch (UserNotFoundException e) {
            currentUser = userService.findByUuidEntity(username);
        }

        board.setUsersId(currentUser.getId());
        board.setCategory("parentingInformation");
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/parenting/list";
    }

    /** 단일 게시글 상세 페이지를 보여줍니다. */
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
        return "community/parentingDetail";
    }

    /** 댓글을 저장하고 상세 페이지로 리다이렉트합니다. */
    @PostMapping("/{id:[0-9]+}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication authentication
    ) {
        // principal → Users
        String username = authentication.getName();
        Users currentUser;
        try {
            currentUser = userService.findByUsernameEntity(username);
        } catch (UserNotFoundException e) {
            currentUser = userService.findByUuidEntity(username);
        }

        Comment comment = new Comment();
        comment.setCommunityId(id);
        comment.setUsersId(currentUser.getId());
        comment.setContent(content);
        commentService.save(comment);
        return "redirect:/community/parenting/" + id;
    }

    /** 게시글 수정 폼을 보여줍니다. */
    @GetMapping("/{id:[0-9]+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board", board);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "community/parentingForm";
    }

    /** 게시글 수정을 처리하고 상세 페이지로 리다이렉트합니다. */
    @PostMapping("/{id:[0-9]+}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board
    ) {
        CommunityBoard old = boardService.findById(id);
        // usersId 는 기존 작성자 유지
        board.setUsersId(old.getUsersId());
        old.setCategory2(board.getCategory2());
        old.setAgeGroup(board.getAgeGroup());
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/community/parenting/" + id;
    }

    /** 게시글 삭제를 처리하고 리스트 페이지로 리다이렉트합니다. */
    @PostMapping("/{id:[0-9]+}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/community/parenting/list";
    }
}
