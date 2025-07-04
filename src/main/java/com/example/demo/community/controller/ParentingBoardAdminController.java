package com.example.demo.community.controller;

import com.example.demo.community.entity.Comment;
import com.example.demo.community.entity.CommunityBoard;
import com.example.demo.community.service.CommentService;
import com.example.demo.community.service.CommunityBoardService;
import com.example.demo.enums.AgeGroup;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/community/parenting")
@RequiredArgsConstructor
public class ParentingBoardAdminController {

    private final CommunityBoardService boardService;
    private final CommentService commentService;
    private final UserService userService;

    private final List<String> subCategories = Arrays.asList("수유","수면","위생","발달","건강");
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(a->a!=AgeGroup.ALL && a!=AgeGroup.VARIOUS)
            .collect(Collectors.toList());

    // 1) 목록 (필터/페이징/정렬 그대로)
    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(defaultValue="전체") String subCategory,
                       @RequestParam(defaultValue="ALL") AgeGroup ageGroup,
                       @RequestParam(defaultValue="") String keyword,
                       @RequestParam(defaultValue="popular") String sort,
                       @RequestParam(defaultValue="0") int page) {
        Page<CommunityBoard> boards = boardService.getList(
                "parentingInformation", subCategory, ageGroup, keyword, sort, page
        );
        model.addAttribute("boards", boards.getContent());
        model.addAttribute("pageData", boards);
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("ageGroup", ageGroup);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "admin/community/parentingList";
    }

    // 2) 상세 (수정·삭제 가능)
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(defaultValue="0") int commentPage,
                         Model model) {
        // 조회수
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews()+1);
        boardService.save(board);

        // 글
        model.addAttribute("board", board);
        model.addAttribute("authorNickname",
                userService.findNicknameOrDefault(board.getUsersId()));

        // 댓글
        Page<Comment> commentsPage = commentService.getCommentsPage(id, commentPage);
        List<String> commentNicknames = commentsPage.getContent().stream()
                .map(c -> userService.findNicknameOrDefault(c.getUsersId()))
                .collect(Collectors.toList());
        model.addAttribute("commentList", commentsPage.getContent());
        model.addAttribute("commentNicknames", commentNicknames);
        model.addAttribute("commentPageData",  commentsPage);

        return "admin/community/parentingDetail";
    }

    // 3) 글 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("board", boardService.findById(id));
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups", ageGroups);
        return "admin/community/parentingForm";
    }

    // 4) 글 수정 처리
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute CommunityBoard board) {
        CommunityBoard old = boardService.findById(id);
        old.setCategory2(board.getCategory2());
        old.setAgeGroup(board.getAgeGroup());
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/admin/community/parenting/" + id;
    }

    // 5) 글 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/admin/community/parenting/list";
    }

    // 6) 댓글 수정 처리
    @PostMapping("/{boardId}/comments/{commentId}")
    public String updateComment(@PathVariable Long boardId,
                                @PathVariable Long commentId,
                                @RequestParam String content) {
        Comment c = commentService.findById(commentId);
        c.setContent(content);
        commentService.save(c);
        return "redirect:/admin/community/parenting/" + boardId;
    }

    // 7) 댓글 삭제
    @PostMapping("/{boardId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long boardId,
                                @PathVariable Long commentId) {
        commentService.delete(commentId);
        return "redirect:/admin/community/parenting/" + boardId;
    }
}
