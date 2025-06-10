package com.example.demo.survey.controller;

import com.example.demo.survey.dto.RecordAnswerRequest;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.survey.service.RecordAnswerService;
import com.example.demo.survey.service.RecordSurveyService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.mapper.RecordAnswerMapper;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.ChildService;
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
    private final MemberRepository memberRepository;

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

//    @PostMapping("/batch")
//    public ResponseEntity<?> submitBatch(@RequestBody List<RecordAnswerRequest> requestList) {
//        System.out.println("== Batch Submit Start ==");
//
//        for (RecordAnswerRequest request : requestList) {
//            if (request.getSurveyId() == null || request.getChildId() == null) {
//                throw new IllegalArgumentException("surveyId 또는 childId 누락");
//            }
//
//            RecordSurvey survey = recordSurveyService.get(request.getSurveyId());
//            Child child = childService.get(request.getChildId());
//
//            Member mockMember = memberRepository.findById(1L)
//                    .orElseThrow(() -> new IllegalArgumentException("임시 유저 없음"));
//
//            RecordAnswer entity = RecordAnswerMapper.toEntity(request, survey, mockMember, child);
//            recordAnswerService.create(entity);
//        }
//
//        return ResponseEntity.ok().build();
//    }

}
