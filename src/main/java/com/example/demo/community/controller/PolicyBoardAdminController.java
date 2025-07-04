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
@RequestMapping("/admin/community/policy")
@RequiredArgsConstructor
public class PolicyBoardAdminController {

    private final CommunityBoardService boardService;
    private final CommentService    commentService;
    private final UserService       userService;

    // 정책정보 전용 하위카테고리
    private final List<String> subCategories = Arrays.asList(
            "보육지원", "출산지원", "건강검진", "양육휴가", "재정지원"
    );
    // 연령대 필터 (ALL, VARIOUS 제외)
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(a -> a != AgeGroup.ALL && a != AgeGroup.VARIOUS)
            .collect(Collectors.toList());

    /** 1) 목록 (필터/검색/정렬/페이징) */
    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(defaultValue = "전체") String subCategory,
            @RequestParam(defaultValue = "ALL") AgeGroup ageGroup,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "popular") String sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<CommunityBoard> boards = boardService.getList(
                "policyInformation",
                subCategory, ageGroup, keyword, sort, page
        );

        model.addAttribute("boards",       boards.getContent());
        model.addAttribute("pageData",     boards);
        model.addAttribute("subCategory",  subCategory);
        model.addAttribute("ageGroup",     ageGroup);
        model.addAttribute("keyword",      keyword);
        model.addAttribute("sort",         sort);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups",     ageGroups);

        return "admin/community/policyList";
    }

    /** 2) 상세 (조회수↑, 수정/삭제/댓글 가능) */
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int commentPage,
            Model model
    ) {
        // 조회수 업데이트
        CommunityBoard board = boardService.findById(id);
        board.setViews(board.getViews() + 1);
        boardService.save(board);

        // 글 정보
        model.addAttribute("board", board);
        model.addAttribute("authorNickname",
                userService.findNicknameOrDefault(board.getUsersId()));

        // 댓글 페이징 & 닉네임
        Page<Comment> commentsPage = commentService.getCommentsPage(id, commentPage);
        List<String> commentNicknames = commentsPage.getContent().stream()
                .map(c -> userService.findNicknameOrDefault(c.getUsersId()))
                .toList();

        model.addAttribute("commentList",      commentsPage.getContent());
        model.addAttribute("commentNicknames", commentNicknames);
        model.addAttribute("commentPageData",  commentsPage);

        return "admin/community/policyDetail";
    }

    /** 3) 글 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute("board",         boardService.findById(id));
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("ageGroups",     ageGroups);
        return "admin/community/policyForm";
    }

    /** 4) 글 수정 처리 */
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute CommunityBoard board
    ) {
        CommunityBoard old = boardService.findById(id);
        old.setCategory2(board.getCategory2());
        old.setAgeGroup(board.getAgeGroup());
        old.setTitle(board.getTitle());
        old.setContent(board.getContent());
        boardService.save(old);
        return "redirect:/admin/community/policy/" + id;
    }

    /** 5) 글 삭제 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/admin/community/policy/list";
    }

    /** 6) 댓글 수정 처리 */
    @PostMapping("/{boardId}/comments/{commentId}")
    public String updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestParam String content
    ) {
        Comment c = commentService.findById(commentId);
        c.setContent(content);
        commentService.save(c);
        return "redirect:/admin/community/policy/" + boardId;
    }

    /** 7) 댓글 삭제 */
    @PostMapping("/{boardId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId
    ) {
        commentService.delete(commentId);
        return "redirect:/admin/community/policy/" + boardId;
    }
}
