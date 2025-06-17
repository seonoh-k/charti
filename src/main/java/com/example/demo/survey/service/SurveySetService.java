//package com.example.demo.survey.service;
//
//import com.example.demo.survey.dto.SurveySetSearchDto;
//import com.example.demo.survey.dto.SurveySetForm;
//import com.example.demo.survey.entity.*;
//import com.example.demo.survey.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class SurveySetService {
//    private final SurveySetRepository     surveySetRepo;
//    private final GroupSurveyRepository   groupRepo;
//    private final SpecialSurveyRepository specialRepo;
//
//    /** 목록 + 필터 + 페이징 */
//    public Page<SurveySet> list(SurveySetSearchDto dto, Pageable pageable) {
//        Specification<SurveySet> spec = Specification.where(null);
//
//        if (StringUtils.hasText(dto.getKeyword())) {
//            spec = spec.and((r, q, cb) -> cb.like(r.get("setTitle"), "%" + dto.getKeyword() + "%"));
//        }
//        if (!"all".equals(dto.getAgeGroup())) {
//            spec = spec.and((r, q, cb) -> cb.equal(r.get("ageGroup"), dto.getAgeGroup()));
//        }
//        if (!"all".equals(dto.getCategory())) {
//            spec = spec.and((r, q, cb) -> cb.equal(r.get("category"), dto.getCategory()));
//        }
//        if (!"all".equals(dto.getType())) {
//            spec = spec.and((r, q, cb) -> cb.equal(r.get("type"), dto.getType()));
//        }
//        return surveySetRepo.findAll(spec, pageable);
//    }
//
//    /** 상세 조회 */
//    // 세트 정보만 조회
//    public SurveySet getDetail(Long id) {
//        return surveySetRepo.findById(id)
//                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 세트: " + id));
//    }
//
//    // 이 세트에 속한 문진만 직접 조회
//    @Transactional(readOnly = true)
//    public List<? extends BaseSurvey> getSurveysBySetId(Long setId) {
//        SurveySet set = getDetail(setId);
//        return "GROUP".equalsIgnoreCase(set.getType())
//                ? groupRepo.findBySurveySetId(setId)
//                : specialRepo.findBySurveySetId(setId);
//    }
//
//    /** 생성/수정 */
//    public SurveySet createOrUpdate(SurveySetForm form) {
//        // 1) 설문 로드
//        List<? extends BaseSurvey> surveys = "GROUP".equals(form.getType())
//                ? groupRepo.findAllById(form.getSurveyIds())
//                : specialRepo.findAllById(form.getSurveyIds());
//
//        // 2) 연령대/카테고리 계산
//        Set<String> ages      = surveys.stream().map(BaseSurvey::getAgeGroup).collect(Collectors.toSet());
//        Set<String> categories= surveys.stream().map(BaseSurvey::getCategory).collect(Collectors.toSet());
//        String ag = ages.size()==1? ages.iterator().next(): "various";
//        String ca = categories.size()==1? categories.iterator().next(): "various";
//
//        if ("various".equals(ag) && "various".equals(ca)) {
//            throw new IllegalArgumentException("문진 세트는 카테고리 또는 연령대를 같게 선택해주세요.");
//        }
//
//        // 3) 엔티티 준비
//        SurveySet entity = form.getId()==null? new SurveySet(): getDetail(form.getId());
//        entity.setSetTitle(form.getSetTitle());
//        entity.setType(form.getType());
//        entity.setAgeGroup(ag);
//        entity.setCategory(ca);
//
//        // 4) 연관관계 설정
//        entity.getGroupSurveys().clear();
//        entity.getSpecialSurveys().clear();
//        if ("GROUP".equals(form.getType())) {
//            surveys.forEach(s -> entity.getGroupSurveys().add((GroupSurvey) s));
//        } else {
//            surveys.forEach(s -> entity.getSpecialSurveys().add((SpecialSurvey) s));
//        }
//        return surveySetRepo.save(entity);
//    }
//
//    /** 폼용 전체 설문 조회 (필터 적용 가능) */
//    public List<GroupSurvey> allGroup(String age, String category) {
//        return groupRepo.findAll().stream()
//                .filter(s -> ("all".equals(age) || s.getAgeGroup().equals(age)))
//                .filter(s -> ("all".equals(category) || s.getCategory().equals(category)))
//                .collect(Collectors.toList());
//    }
//
//    public List<SpecialSurvey> allSpecial(String age, String category) {
//        return specialRepo.findAll().stream()
//                .filter(s -> ("all".equals(age) || s.getAgeGroup().equals(age)))
//                .filter(s -> ("all".equals(category) || s.getCategory().equals(category)))
//                .collect(Collectors.toList());
//    }
//}