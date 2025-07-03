package com.example.demo.matching.controller;

import com.example.demo.enums.SurveyCategory;
import com.example.demo.matching.dto.MatchingRequestDto;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.entity.MatchingAnswer;
import com.example.demo.matching.service.MatchingAnswerService;
import com.example.demo.matching.service.MatchingService;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.repository.SpecialAnswerRepository;
import com.example.demo.survey.repository.GroupAnswerRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.service.PresignedUrlService;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/matching")
@RequiredArgsConstructor
@Slf4j
public class MatchingController {

    private final MatchingService         matchingService;
    private final ChildRepository         childRepository;
    private final SpecialAnswerRepository specialAnswerRepository;
    private final GroupAnswerRepository   groupAnswerRepository;
    private final PresignedUrlService     urlService;
    private final MatchingAnswerService answerService;
    private final MemberRepository memberRepository;


    /** 1) SurveyCategory별 상담 신청 폼 */
    @GetMapping("/{category}/request")
    public String showRequestForm(
            @PathVariable SurveyCategory category,
            @RequestParam("type") MatchingRequestDto.AnswerType type,
            @RequestParam("childId") Long childId,
            @RequestParam("answerId") List<Long> answerIds,
            Model model) {
        log.info("answerIds : {}", answerIds);
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 자녀 ID입니다."));

        // 타입에 따라 답변 조회
        List<SpecialAnswer> answers = new ArrayList<>();
        if (type == MatchingRequestDto.AnswerType.SPECIAL) {
            answers = specialAnswerRepository.findByIdIn(answerIds);
        } else {
            answers = groupAnswerRepository.findAllById(answerIds).stream()
                    .map(ga -> {
                        SpecialAnswer sa = new SpecialAnswer();
                        // groupAnswer의 PK를 SpecialAnswer의 id에도 복사해야
                        // 폼의 hidden field로 value가 넘어갑니다.
                        sa.setId(ga.getId());
                        sa.setChild(ga.getChild());
                        sa.setSurveySet(ga.getSurveySet());
                        sa.setAgeGroup(ga.getAgeGroup());
                        sa.setCategory(ga.getCategory());
                        sa.setQuestion(ga.getQuestion());
                        sa.setAnswer(ga.getAnswer());
                        return sa;
                    })
                    .toList();
        }
        if (answers.isEmpty())
            throw new IllegalArgumentException("문의 대상이 비어있습니다.");

        // 폼 초기화
        MatchingRequestDto form = new MatchingRequestDto();
        form.setType(type);
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
            @ModelAttribute MatchingRequestDto req
    ) throws Exception {
        // 1) 기본 유효성
        var type = req.getType();
        if (req.getChildId() == null || req.getAnswerIds().isEmpty() || type == null) {
            throw new IllegalArgumentException("자녀, 문의 대상, 타입이 비어있습니다.");
        }
        Child child = childRepository.findById(req.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 자녀 정보입니다."));

        // ① 타입에 따라 다시 조회
        List<SpecialAnswer> answers;
        if (type == MatchingRequestDto.AnswerType.SPECIAL) {
            answers = specialAnswerRepository.findAllById(req.getAnswerIds());
        } else {
            answers = groupAnswerRepository.findAllById(req.getAnswerIds()).stream()
                    .map(ga -> {
                        SpecialAnswer sa = new SpecialAnswer();
                        sa.setChild(ga.getChild());
                        sa.setSurveySet(ga.getSurveySet());
                        sa.setAgeGroup(ga.getAgeGroup());
                        sa.setCategory(ga.getCategory());
                        sa.setQuestion(ga.getQuestion());
                        sa.setAnswer(ga.getAnswer());
                        return sa;
                    })
                    .toList();
        }
        if (answers.isEmpty())
            throw new IllegalArgumentException("문의 대상 정보가 비어있습니다.");

        // ② 매칭 생성
        Matching m = new Matching();
        m.setChild(child);
        m.setCategory(req.getCategory());
        m.setTitle(req.getTitle());
        m.setContent(req.getContent());
        answers.forEach(a -> a.setMatching(m));
        m.getAnswers().addAll(answers);

        // 3) 이미지 업로드
        MultipartFile img = req.getImage();
        if (img != null && !img.isEmpty()) {
            String origName = img.getOriginalFilename();
            String mimeType = img.getContentType();
            String filename = UUID.randomUUID() + "_" + origName;

            // 3-1) 원본 업로드
            byte[] bytes = img.getBytes();
            InputStream origIn = new ByteArrayInputStream(bytes);
            urlService.imageUpload("original/" + filename, origIn, mimeType);

            // 3-2) 썸네일 생성 & 업로드
            ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(bytes))
                    .width(400)
                    .outputFormat("jpeg")
                    .outputQuality(0.5)
                    .toOutputStream(thumbOut);
            urlService.imageUpload(
                    "thumbnail/" + filename,
                    new ByteArrayInputStream(thumbOut.toByteArray()),
                    "image/jpeg"
            );

            // 3-3) 엔티티에 파일명 저장
            m.setFilename(filename);
        }

        // 4) 저장
        matchingService.save(m);

        return "redirect:/matching";
    }

    /** 4) 부모용 상담 리스트 */
    @GetMapping
    public String parentList(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) Long childId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Authentication auth,
            Model model
    ) {
        // 1) 로그인한 부모(Member) 조회
        Member parent = memberRepository.findByUsersUuid(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("부모 정보가 없습니다."));
        Long parentId = parent.getId();

        // 2) 페이지 요청
        PageRequest pr = PageRequest.of(page, 10, Sort.by("createdAt").descending());

        // 3) 상담 조회
        Page<Matching> matchings =
                matchingService.findForParent(parentId, status, childId, keyword, pr);

        // 4) 자녀 목록 (필터용)
        List<Child> children = childRepository.findByParent_Id(parentId);

        model.addAttribute("matchings",      matchings);
        model.addAttribute("statuses",       List.of("ALL","REQUESTED","MATCHED","RESPONDED"));
        model.addAttribute("currentStatus",  status);
        model.addAttribute("children",       children);
        model.addAttribute("currentChildId", childId);
        model.addAttribute("keyword",        keyword);

        return "matching/list";
    }


    /** 4) 부모용 상세보기 */
    @GetMapping("/detail/{id}")
    public String parentDetail(
            @PathVariable Long id,
            Model model
    ) {
        Matching matching = matchingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 상담 ID입니다."));
        model.addAttribute("matching", matching);

        // 사진 리스트
        List<String> photos;
        String csv = matching.getFilename();
        if (csv != null && !csv.isBlank()) {
            photos = List.of(csv.split("\\s*,\\s*"));
        } else {
            photos = List.of();
        }
        model.addAttribute("photos", photos);

        // 전문가 답변이 있다면 따로 서비스에서 조회
        List<MatchingAnswer> expertAnswers = answerService.findByMatchingId(id);
        model.addAttribute("expertAnswers", expertAnswers);

        return "matching/detail";
    }
}
