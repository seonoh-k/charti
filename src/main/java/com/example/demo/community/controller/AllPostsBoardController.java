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

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/community/allPosts")
public class AllPostsBoardController {

    private final CommunityBoardService boardService;
    private final CommentService         commentService;
    private final UserService            userService;

    public AllPostsBoardController(CommunityBoardService boardService,
                                   CommentService commentService,
                                   UserService userService) {
        this.boardService   = boardService;
        this.commentService = commentService;
        this.userService    = userService;
    }

    // 전체글 리스트: 검색, 정렬(popular/new), 페이징
    @GetMapping({"/list", "/list/"})
    public String list(
            Model model,
            @RequestParam(defaultValue = "") String mainCategory,
            @RequestParam(defaultValue = "전체") String subCategory,
            @RequestParam(defaultValue = "ALL")  AgeGroup ageGroup,
            @RequestParam(defaultValue = "")    String keyword,
            @RequestParam(name = "sort", defaultValue = "popular") String sortOrder,
            @RequestParam(defaultValue = "0")   int page
    ) {
        Page<CommunityBoard> result = boardService.getList(
                mainCategory, subCategory, ageGroup, keyword, sortOrder, page);

        model.addAttribute("boards",      result.getContent());
        model.addAttribute("pageData",    result);
        model.addAttribute("sort",        sortOrder);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("ageGroup",    ageGroup);
        return "community/allPostsList";
    }

    // 상세 페이지 (조회수 증가 + 댓글 페이징)
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
        String authorNickname = userService.findNicknameOrDefault(board.getUsersId());

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
        model.addAttribute("authorNickname", authorNickname);
        model.addAttribute("commentsPage",      commentsPage);
        model.addAttribute("commentNicknames",  commentNicknames);
        model.addAttribute("commentUserIds",    commentUserIds);
        model.addAttribute("currentUserId",     currentUserId);
        return "community/allPostsDetail";
    }

    // 댓글 등록 (여기에 usersId 를 채워서 저장)
    @PostMapping("/{id:[0-9]+}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication auth
    ) {
        // 로그인된 사용자의 principal(이메일 또는 UUID) 가져오기
        String principal = auth.getName();
        Users me;
        try {
            me = userService.findByUsernameEntity(principal);
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(principal);
        }

        // 댓글에 usersId 세팅
        Comment comment = new Comment();
        comment.setCommunityId(id);
        comment.setUsersId(me.getId());
        comment.setContent(content);

        commentService.save(comment);
        return "redirect:/community/allPosts/" + id;
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
        return "redirect:/community/allPosts/" + boardId;
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
        return "redirect:/community/allPosts/" + boardId;
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
