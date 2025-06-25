package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Qna;
import com.example.demo.enums.QnaCategory;
import com.example.demo.service.QnaService;
import com.example.demo.users.entity.Users;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.UserService;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QnaAPIController {

    private final AuthService authService;
    private final QnaService qnaService;
    private final UserService userService;

    @PostMapping("/api/qna/create")
    public ResponseEntity<ApiResponse> createQna(@RequestParam("category")QnaCategory category,
                                                 @RequestParam("title") String title,
                                                 @RequestParam("content") String content) {

        UserDTO userDTO = authService.getLoginUser();
        Users user  = userService.dtoToEntity(userDTO);

        Qna qna = new Qna();
        qna.setUsers(user);
        qna.setCategory(category);
        qna.setTitle(title);
        qna.setContent(content);
        Long id = qnaService.createQna(qna);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK, id));
    }

    @PostMapping("/api/qna/update")
    public ResponseEntity<ApiResponse> updateQna(@RequestParam("id") Long id,
                                                 @RequestParam("category")QnaCategory category,
                                                 @RequestParam("title") String title,
                                                 @RequestParam("content") String content) {

        Qna qna = qnaService.get(id);
        qna.setCategory(category);
        qna.setTitle(title);
        qna.setContent(content);
        qnaService.update(qna);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }

    @GetMapping("/api/qna/delete?id={id}")
    public ResponseEntity<ApiResponse> deleteQna(@PathVariable("id") Long id) {
        qnaService.delete(id);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }

    @PostMapping("/api/qna/visibility")
    public ResponseEntity<ApiResponse> updateQnaVisibility(@RequestParam("id") Long id,
                                                           @RequestParam("isPublic") boolean isPublic) {

        Qna qna = qnaService.get(id);
        qna.setPublic(isPublic);
        qnaService.update(qna);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }
}
