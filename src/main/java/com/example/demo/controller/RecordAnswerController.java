package com.example.demo.controller;

import com.example.demo.dto.RecordAnswerRequest;
import com.example.demo.dto.RecordAnswerResponse;
import com.example.demo.entity.Child;
import com.example.demo.entity.Member;
import com.example.demo.entity.RecordAnswer;
import com.example.demo.entity.RecordSurvey;
import com.example.demo.mapper.RecordAnswerMapper;
import com.example.demo.service.ChildService;
import com.example.demo.service.RecordAnswerService;
import com.example.demo.service.RecordSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/record-answers")
@RequiredArgsConstructor
public class RecordAnswerController {

    private final RecordAnswerService recordAnswerService;
    private final RecordSurveyService recordSurveyService;
    private final ChildService childService;

    // 기록 문진 답변 저장
    @PostMapping
    public ResponseEntity<RecordAnswerResponse> create(@RequestBody RecordAnswerRequest request,
                                                       @RequestAttribute("currentMember") Member currentUser) {
        RecordSurvey survey = recordSurveyService.get(request.getSurveyId());
        Child child = childService.get(request.getChildId());

        RecordAnswer entity = RecordAnswerMapper.toEntity(request, survey, currentUser, child);
        recordAnswerService.create(entity);

        return ResponseEntity.ok(RecordAnswerMapper.toResponse(entity));
    }

    // 본인 작성 전체 답변 조회
    @GetMapping("/me")
    public ResponseEntity<List<RecordAnswerResponse>> getMyAnswers(@RequestAttribute("currentMember") Member currentUser) {
        List<RecordAnswer> answers = recordAnswerService.getAnswersByWriter(currentUser);
        return ResponseEntity.ok(answers.stream().map(RecordAnswerMapper::toResponse).toList());
    }

    // 본인의 특정 자녀에 대한 답변만 조회
    @GetMapping("/me/child/{childId}")
    public ResponseEntity<List<RecordAnswerResponse>> getMyChildAnswers(@RequestAttribute("currentMember") Member currentUser,
                                                                        @PathVariable Long childId) {
        Child child = childService.get(childId);
        List<RecordAnswer> answers = recordAnswerService.getAnswersByWriterAndChild(currentUser, child);
        return ResponseEntity.ok(answers.stream().map(RecordAnswerMapper::toResponse).toList());
    }

    // 답변 소프트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recordAnswerService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
