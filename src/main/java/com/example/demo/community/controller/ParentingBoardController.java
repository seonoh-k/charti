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
        this.boardService   = boardService;
        this.commentService = commentService;
        this.userService    = userService;
    }

    private final List<String> subCategories = Arrays.asList(
            "수유", "수면", "위생", "발달", "건강"
    );
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(age -> age != AgeGroup.ALL && age != AgeGroup.VARIOUS)
            .collect(Collectors.toList());

    @GetMapping({"/list","/list/"})
    public String list(
            Model model,
            @RequestParam(defaultValue="전체") String subCategory,
            @RequestParam(defaultValue="ALL") AgeGroup ageGroup,
            @RequestParam(defaultValue="") String keyword,
            @RequestParam(name="sort", defaultValue="popular") String sortOrder,
            @RequestParam(defaultValue="0") int page
    ) {
        Page<CommunityBoard> result = boardService.getList(
                "parentingInformation", subCategory, ageGroup, keyword, sortOrder, page
        );
        model.addAttribute("boards",        result.getContent());
        model.addAttribute("pageData",      result);
        model.addAttribute("subCategory",   subCategory);
        model.addAttribute("ageGroup",      ageGroup);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("sort",          sortOrder);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups",     ageGroups);
        return "community/parentingList";
    }

    @GetMapping("/new")
    public String form(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/loginForm";
        }
        model.addAttribute("board",         new CommunityBoard());
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups",     ageGroups);
        return "community/parentingForm";
    }

    @PostMapping("")
    public String create(
            @ModelAttribute CommunityBoard board,
            Authentication auth
    ) {
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
        board.setUsersId(me.getId());
        board.setCategory("parentingInformation");
        board.setStatus("Y");
        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/community/parenting/list";
    }

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

        // 3) 글 작성자 닉네임
        String authorNickname = userService.findNicknameOrDefault(board.getUsersId());

        // 4) 댓글 작성자 닉네임 리스트
        List<String> commentNicknames = commentsPage.getContent().stream()
                .map(c -> userService.findNicknameOrDefault(c.getUsersId()))
                .collect(Collectors.toList());

        // 5) 로그인한 사용자 ID (수정/삭제 버튼용)
        Long currentUserId = null;
        if (auth != null && auth.isAuthenticated()) {
            try {
                Users u = userService.findByUsernameEntity(auth.getName());
                currentUserId = u.getId();
            } catch (UserNotFoundException e) {
                currentUserId = userService.findByUuidEntity(auth.getName()).getId();
            }
        }

        model.addAttribute("board",            board);
        model.addAttribute("commentsPage",     commentsPage);
        model.addAttribute("authorNickname",   authorNickname);
        model.addAttribute("commentNicknames", commentNicknames);
        model.addAttribute("currentUserId",    currentUserId);
        return "community/parentingDetail";
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
        Comment comment = new Comment();
        comment.setCommunityId(id);
        comment.setUsersId(me.getId());
        comment.setContent(content);
        commentService.save(comment);
        return "redirect:/community/parenting/" + id;
    }

    @GetMapping("/{id:[0-9]+}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CommunityBoard board = boardService.findById(id);
        model.addAttribute("board",         board);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups",     ageGroups);
        return "community/parentingForm";
    }

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
        return "redirect:/community/parenting/" + id;
    }

    @PostMapping("/{id:[0-9]+}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/community/parenting/list";
    }
}
