package com.example.demo.matching.controller;

import com.example.demo.entity.Photo;
import com.example.demo.enums.MatchingStatus;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.matching.dto.MatchingRequestDto;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.entity.MatchingAnswer;
import com.example.demo.matching.service.MatchingAnswerService;
import com.example.demo.matching.service.MatchingService;
import com.example.demo.repository.PhotoRepository;
import com.example.demo.service.PhotoService;
import com.example.demo.service.PresignedUrlService;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.repository.SpecialAnswerRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.users.repository.ExpertRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final ChildRepository childRepository;
    private final SpecialAnswerRepository specialAnswerRepository;
    private final ExpertRepository expertRepository;
    private final MatchingAnswerService answerService;

    private final PresignedUrlService urlService;
    private final PhotoRepository photoRepository;


    /** 1) SurveyCategory별 상담 신청 폼 */
    @GetMapping("/{category}/request")
    public String showRequestForm(
            @PathVariable SurveyCategory category,
            @RequestParam("childId") Long childId,
            @RequestParam("answerId") List<Long> answerIds,
            Model model) {

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 자녀 ID입니다."));
        List<SpecialAnswer> answers = specialAnswerRepository.findAllById(answerIds);
        if (answers.isEmpty()) throw new IllegalArgumentException("문의 대상이 비어있습니다.");

        MatchingRequestDto form = new MatchingRequestDto();
        form.setChildId(childId);
        form.setCategory(category);
        form.setAnswerIds(answerIds);

        model.addAttribute("child", child);
        model.addAttribute("answers", answers);
        model.addAttribute("matchingRequest", form);

        return "matching/request";
    }

    /** 2) 상담 신청 처리 */
    @PostMapping
    public String submitRequest(
            @ModelAttribute MatchingRequestDto req,
            @RequestParam(name = "images", required = false) List<MultipartFile> images
    ) throws Exception {
        // 유효성 검증
        if (req.getChildId() == null || req.getAnswerIds().isEmpty()) {
            throw new IllegalArgumentException("자녀 또는 문의 대상이 비어있습니다.");
        }
        Child child = childRepository.findById(req.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 자녀 정보입니다."));
        List<SpecialAnswer> answers = specialAnswerRepository.findAllById(req.getAnswerIds());
        if (answers.isEmpty()) {
            throw new IllegalArgumentException("문의 대상 정보가 비어있습니다.");
        }


        // --- Matching 생성 & 저장 ---
        Matching m = new Matching();
        m.setChild(child);
        m.setCategory(req.getCategory());
        m.setTitle(req.getTitle());
        m.setContent(req.getContent());
        answers.forEach(a -> a.setMatching(m));
        m.getAnswers().addAll(answers);
        matchingService.save(m);

        // --- 이미지 업로드 & DB 저장 ---
        if (images != null) {
            for (MultipartFile img : images) {
                if (img.isEmpty()) continue;

                String origName = img.getOriginalFilename();
                String mimeType = img.getContentType();
                String filename = UUID.randomUUID() + "_" + origName;

                // 원본 업로드
                InputStream origIn = img.getInputStream();
                urlService.imageUpload("original/" + filename, origIn, mimeType);

                // 썸네일 업로드
                ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
                Thumbnails.of(img.getInputStream())
                        .width(400)
                        .outputFormat("jpeg")
                        .outputQuality(0.5)
                        .toOutputStream(thumbOut);
                urlService.imageUpload(
                        "thumbnail/" + filename,
                        new ByteArrayInputStream(thumbOut.toByteArray()),
                        "image/jpeg"
                );

                // DB 저장
                Photo photo = new Photo();
                photo.setFileName(filename);
                photo.setMatching(m);
                photoRepository.save(photo);
            }
        }

        return "redirect:/";
    }

    /** 3) 부모용 상세보기 (상담 & 답변 & —REQUESTED— 시에만 전문가 배정용 list 전달) */
    @GetMapping("/detail/{id}")
    public String parentDetail(
            @PathVariable Long id,
            Model model
    ) {
        Matching matching = matchingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 상담 ID입니다."));
        List<MatchingAnswer> answers = answerService.findByMatchingId(id);

        model.addAttribute("matching", matching);
        model.addAttribute("answers", answers);
        model.addAttribute("photos", matching.getPhotos());

        if (matching.getStatus() == com.example.demo.enums.MatchingStatus.REQUESTED) {
            List<Expert> experts = expertRepository.findAllByMajor(matching.getCategory().getDisplayName());
            model.addAttribute("experts", experts);
        }

        return "matching/detail";
    }

}
