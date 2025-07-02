package com.example.demo.users.service;

import com.example.demo.dto.ChildDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.dto.request.ChildCreateRequest;
import com.example.demo.dto.request.ChildUpdateRequest;
import com.example.demo.entity.Group;
import com.example.demo.exception.ChildNotFoundException;
import com.example.demo.repository.GroupRepository;
import com.example.demo.service.BaseService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.repository.ChildQueryRepository;
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.users.repository.MemberRepository;
import groovy.util.logging.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ChildService extends BaseService<Child, ChildRepository> {


    private ChildQueryRepository childQueryRepository;
    private GroupRepository groupRepository;
    private AuthService authService;
    private MemberRepository memberRepository;
    public ChildService(ChildRepository repository,ChildQueryRepository childQueryRepository,
                        GroupRepository groupRepository, AuthService authService, MemberRepository memberRepository) {
        super(repository);
        this.childQueryRepository = childQueryRepository;
        this.groupRepository = groupRepository;
        this.authService = authService;
        this.memberRepository = memberRepository;
    }
//        extends BaseService<Child, ChildRepository> {

    @Transactional
    public void createChild(ChildCreateRequest dto) {


        Child child = new Child();
        child.setName(dto.getName());
        child.setNickname(dto.getNickname());
        // birthday String -> LocalDateTime
        LocalDate birth = LocalDate.parse(dto.getBirthday());
        child.setBirthday(birth.atStartOfDay());
        child.setGender(dto.getGender());
        child.setHeight(dto.getHeight());
        child.setWeight(dto.getWeight());
        child.setBirthOrder(dto.getBirthOrder());
        UserDTO loginUser = authService.getLoginUser();
        Long memberId = loginUser.getId(); // DTO에서 ID 뽑기

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        child.setParent(member); // 관계 맵핑 정상!

        // 그룹(기관) 연결
        if(dto.getGroupId() != null){
            Group group = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 기관이 존재하지 않습니다."));
            child.setGroup(group);
        }

        child.setDeleted(false);
        child.setRiskGroup(false);


        repository.save(child);
    }

    @Transactional
    public void updateChild(Long id, ChildUpdateRequest dto) {
        Child child = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 자녀가 없습니다."));
        child.setName(dto.getName());
        child.setNickname(dto.getNickname());
        child.setBirthday(dto.getBirthday().atStartOfDay());
        child.setWeight(dto.getWeight());
        child.setHeight(dto.getHeight());
        child.setGender(dto.getGender());
        child.setBirthOrder(dto.getBirthOrder());
        // 소속(그룹) 변경
        if (dto.getGroupId() != null) {
            Group group = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new RuntimeException("그룹 없음"));
            child.setGroup(group);
        }

        repository.save(child); // JPA 변경감지면 생략 가능
    }

    @Transactional
    public void softDeleteChild(Long childId) {
        Child child = repository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("자녀 정보가 없습니다."));
        child.setDeleted(true);
        // 변경 감지(dirty checking)로 자동 update됨
    }

    /**
     * [자녀 ID로 자녀 조회]
     *
     * 자녀의 고유 ID(PK)를 기준으로 자녀(Child) 엔티티를 조회합니다.
     *
     * ✅ 사용 예:
     * - 자녀 선택 시 선택된 자녀의 정보를 가져올 때
     * - 기록 문진 등에서 자녀 정보에 접근할 때
     *
     * 예외 처리: 원래 다른방식으로 했다가 통일성 위해서 변경함
     * - 해당 ID에 해당하는 자녀가 존재하지 않으면 `ChildNotFoundException` 예외를 발생시킵니다.
     *
     * @param id 조회할 자녀의 ID (기본키)
     * @return 조회된 자녀(Child) 엔티티
     * @throws ChildNotFoundException 해당 ID의 자녀가 존재하지 않을 경우 발생
     */
    public Child findById(Long id) {
        Optional<Child> byId = repository.findById(id);
        if (byId.isPresent()) {
            return byId.get();
        } else {
            throw new ChildNotFoundException();
        }
    }

    public ChildDTO findChildDtoById(Long id) {
        Child child = findById(id); // 없으면 예외
        return ChildDTO.fromEntity(child);
    }

    public List<Child> findByUsersId(Long usersId) {
        return repository.findByParentUsersIdAndDeletedFalse(usersId);
    }

    /**
     * [회원의 자녀 목록 조회]
     *
     * 특정 보호자(Member)의 자녀(Child) 목록을 조회합니다.
     *
     * ✅ 사용 예:
     * - 사용자 로그인 후, 자신의 자녀 목록을 화면에 출력할 때
     * - 기록 문진, 데일리 문진 등에서 자녀 선택 기능 구현 시
     *
     * @param member 자녀를 조회할 대상 보호자 엔티티
     * @return 해당 보호자와 연관된 모든 자녀 리스트
     */
    public List<Child> getChildrenByMember(Member member) {
        return repository.findByParent(member);
    }

    public PagingResultDTO<ChildDTO, Child> getChildrenByGroup(Long groupId, Pageable pageable) {
        Page<ChildDTO> result = childQueryRepository.getChildDTOsByGroupIdOrderByAgeAndName(groupId, pageable);

        return new PagingResultDTO<>(result);
    }

    /**
     * [자녀를 그룹에서 제외]
     *
     * 특정 자녀(Child)를 현재 소속된 그룹(Group)에서 제외(연결 해제)합니다.
     *
     * ✅ 사용 예:
     * - 담당자(관리자)가 그룹 소속 자녀를 그룹에서 내보내고 싶을 때
     * - 자녀가 더 이상 해당 기관(그룹)에 소속되지 않아야 할 때
     *
     * @param childId 그룹에서 제외할 자녀의 고유 ID
     * @throws RuntimeException 자녀 정보가 없을 경우
     */
    @Transactional
    public void removeChildFromGroup(Long childId) {
        Child child = repository.findById(childId)
                .orElseThrow(() -> new RuntimeException("자녀 정보가 없습니다."));

        child.setGroup(null); // 그룹과의 연관 해제
        repository.save(child);
    }
}
