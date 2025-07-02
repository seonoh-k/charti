package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.dto.SpecialSurveyResponseDto;
import com.example.demo.survey.dto.SurveySetDTO;
import com.example.demo.survey.dto.SurveySetSearchDto;
import com.example.demo.survey.dto.SurveySetForm;
import com.example.demo.survey.entity.*;
import com.example.demo.survey.repository.*;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.repository.ManagerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.example.demo.exception.SurveySetNotFoundException;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SurveySetService {

    private final SurveySetRepository     surveySetRepo;
    private final GroupSurveyRepository   groupRepo;
    private final SpecialSurveyRepository specialRepo;
    private final ManagerRepository managerRepo;

    /** 목록 + 필터 + 페이징 */
    public Page<SurveySet> list(SurveySetSearchDto dto, Pageable pageable) {
        Specification<SurveySet> spec = Specification.where(null);

        if (StringUtils.hasText(dto.getKeyword())) {
            spec = spec.and((r, q, cb) -> cb.like(r.get("setTitle"), "%" + dto.getKeyword() + "%"));
        }
        if (dto.getAgeGroup() != AgeGroup.ALL) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("ageGroup"), dto.getAgeGroup())
            );
        }
        if (dto.getCategory() != SurveyCategory.ALL) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category"), dto.getCategory())
            );
        }

        if (!"all".equals(dto.getType())) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("type"), dto.getType()));
        }
        if (dto.getTargetGroup() != null
                && dto.getTargetGroup() != TargetGroup.ALL) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("targetGroup"), dto.getTargetGroup())
            );
        }
        return surveySetRepo.findAll(spec, pageable);
    }

    /** 상세 조회 */
    // 세트 정보만 조회
    public SurveySet getDetail(Long id) {
        return surveySetRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 세트: " + id));
    }

    // 이 세트에 속한 문진만 직접 조회
    @Transactional(readOnly = true)
    public List<? extends BaseSurvey> getSurveysBySetId(Long setId) {
        SurveySet set = getDetail(setId);
        return "GROUP".equalsIgnoreCase(set.getType())
                ? groupRepo.findBySurveySetId(setId)
                : specialRepo.findBySurveySetId(setId);
    }

    /** 생성/수정 */
    public SurveySet createOrUpdate(SurveySetForm form) {
        // 1) 설문 로드
        List<? extends BaseSurvey> surveys = "GROUP".equals(form.getType())
                ? groupRepo.findAllById(form.getSurveyIds())
                : specialRepo.findAllById(form.getSurveyIds());

        // 2) 연령대/카테고리 계산
        Set<AgeGroup> ages = surveys.stream()
                .map(BaseSurvey::getAgeGroup)
                .collect(Collectors.toSet());
        Set<SurveyCategory> categories = surveys.stream()
                .map(BaseSurvey::getCategory)
                .collect(Collectors.toSet());

        // 단일 연령/카테고리면 해당 값, 멀티플이면 VARIOUS
        AgeGroup ag = (ages.size() == 1)
                ? ages.iterator().next()
                : AgeGroup.VARIOUS;
        SurveyCategory ca = (categories.size() == 1)
                ? categories.iterator().next()
                : SurveyCategory.VARIOUS;

        // “둘 다 VARIOUS”인 경우는 불가
        if (ag == AgeGroup.VARIOUS && ca == SurveyCategory.VARIOUS) {
            throw new IllegalArgumentException(
                    "문진 세트는 연령대 또는 카테고리가 같아야 합니다."
            );
        }

        // 3) 엔티티 준비
        SurveySet entity = form.getId()==null? new SurveySet(): getDetail(form.getId());
        entity.setSetTitle(form.getSetTitle());
        entity.setType(form.getType());
        entity.setAgeGroup(ag);
        entity.setCategory(ca);
        entity.setTargetGroup(form.getTargetGroup());

        // 4) 연관관계 설정
        entity.getGroupSurveys().clear();
        entity.getSpecialSurveys().clear();
        if ("GROUP".equals(form.getType())) {
            surveys.forEach(s -> entity.getGroupSurveys().add((GroupSurvey) s));
        } else {
            surveys.forEach(s -> entity.getSpecialSurveys().add((SpecialSurvey) s));
        }

        if ("GROUP".equals(form.getType())) {
            entity.setTargetGroup(form.getTargetGroup());
        } else {
            entity.setTargetGroup(null);
        }

        return surveySetRepo.save(entity);
    }

    /** 폼용 전체 설문 조회 (필터 적용 가능) */
    public List<GroupSurvey> allGroup(
            AgeGroup age,
            SurveyCategory category,
            TargetGroup targetGroup
    ) {
        return groupRepo.findAll().stream()
                // 연령대 필터
                .filter(s -> age == AgeGroup.ALL || s.getAgeGroup() == age)
                // 카테고리 필터
                .filter(s -> category == SurveyCategory.ALL || s.getCategory() == category)
                // 대상 그룹 필터: ALL 또는 null 이면 모두 통과, 그렇지 않으면 Optional 안 값과 비교
                .filter(s -> {
                    if (targetGroup == null || targetGroup == TargetGroup.ALL) {
                        return true;
                    }
                    // Optional<TargetGroup> 안의 값을 꺼내 비교
                    return s.getTargetGroup()
                            .map(tg -> tg == targetGroup)
                            .orElse(false);
                })
                .collect(Collectors.toList());
    }

    public List<SpecialSurvey> allSpecial(AgeGroup age, SurveyCategory category) {
        return specialRepo.findAll().stream()
                .filter(s -> (age == AgeGroup.ALL || s.getAgeGroup() == age))
                .filter(s -> (category == SurveyCategory.ALL || s.getCategory() == category))
                .collect(Collectors.toList());
    }

    /**
     * [담당자용] 소속 그룹(TargetGroup)에 해당하는 문진 세트 목록 조회
     *
     * @param managerId 현재 로그인한 담당자 ID
     * @return 문진 세트 목록
     */
    public List<SurveySet> getSetsForManager(Long managerId) {
        Manager manager = managerRepo.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저입니다."));

        if (manager.getGroup() == null || manager.getGroup().getTargetGroup() == null) {
            return new ArrayList<>();
        }
        TargetGroup targetGroup = manager.getGroup().getTargetGroup();

        return surveySetRepo.findAllByTargetGroupForManager(targetGroup);
    }
//    public List<SurveySet> getSetsForManager(Long managerId) {
//        Manager manager = managerRepo.findById(managerId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저입니다."));
//
//        if (manager.getGroup() == null || manager.getGroup().getTargetGroup() == null) {
//            return new ArrayList<>();
//        }
//
//        TargetGroup targetGroup = manager.getGroup().getTargetGroup();
//
//        return surveySetRepo.findAllByTargetGroupAndType(targetGroup, "GROUP");
//    }

    /**
     * [문진 세트 ID로 단일 조회]
     *
     * @param setId 문진 세트 ID
     * @return SurveySet 엔티티
     * @throws SurveySetNotFoundException 해당 ID에 해당하는 문진 세트가 없을 경우
     */
    public SurveySet getById(Long setId) {
        return surveySetRepo.findById(setId)
                .orElseThrow(() -> new EntityNotFoundException("설문 세트 없음: " + setId));
    }


    /**
     *  동적 조건에 따라 문진 세트를 조회하는 메소드
     * - 관리자 알림 발송 시, 필터 조건에 맞는 세트 목록을 실시간으로 조회하기 위해 사용됩니다.
     */
    public List<SurveySet> findByCriteria(String type, AgeGroup ageGroup, TargetGroup targetGroup, SurveyCategory category) {

        // 1. Specification: '검색 조건' 자체를 객체로 만드는 방법입니다.
        Specification<SurveySet> spec = (root, query, criteriaBuilder) -> {

            // 2. 여러 조건들을 담을 리스트를 준비합니다.
            List<Predicate> predicates = new ArrayList<>();

            // 3. 각 파라미터 값이 존재할 때만, 검색 조건을 리스트에 추가합니다.
            if (StringUtils.hasText(type)) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (ageGroup != null) {
                predicates.add(criteriaBuilder.equal(root.get("ageGroup"), ageGroup));
            }
            if (targetGroup != null) {
                // SurveySet 엔티티에 targetGroup 필드가 추가되어 있어야 합니다.
                predicates.add(criteriaBuilder.equal(root.get("targetGroup"), targetGroup));
            }
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // 4. 모든 조건들을 'AND'로 묶어서 최종 쿼리를 생성합니다.
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 5. 완성된 조건(spec)으로 JpaRepository에 조회 요청을 보냅니다.
        return surveySetRepo.findAll(spec);
    }

    /** 문진 세트 삭제 */
    public void deleteById(Long setId) {
        SurveySet set = surveySetRepo.findById(setId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 세트: " + setId));

//        // 1) 그룹 문진 세트에 연결된 group_answer 먼저 삭제
//        groupAnswerRepo.deleteBySurveySetId(setId);
//
//        // 2) 특별 문진 세트에 연결된 special_answer 먼저 삭제
//        specialAnswerRepo.deleteBySurveySetId(setId);

        set.getGroupSurveys().clear();
        set.getSpecialSurveys().clear();

        surveySetRepo.delete(set);
    }

    @Transactional
    public SurveySetDTO getSurveySet(AgeGroup age, SurveyCategory category) {
        String title = age.getDisplayName() + " " + category.getDisplayName();
        SurveySet surveySet = surveySetRepo.findBySetTitleAndType(title, "SPECIAL");

        List<SpecialSurvey> specialSurveys = surveySet.getSpecialSurveys();
        List<SpecialSurveyResponseDto> specialSurveyResponseDtos = new ArrayList<>();
        for(SpecialSurvey specialSurvey : specialSurveys) {
            specialSurveyResponseDtos.add(new SpecialSurveyResponseDto(specialSurvey));
        }

        SurveySetDTO surveySetDTO = new SurveySetDTO();
        surveySetDTO.setSetId(surveySet.getSetId());
        surveySetDTO.setSurveyList(specialSurveyResponseDtos);
        return surveySetDTO;

    }
}