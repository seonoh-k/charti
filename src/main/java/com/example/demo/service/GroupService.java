package com.example.demo.service;

import com.example.demo.dto.ChildSimpleDTO;
import com.example.demo.dto.ChildWithParentDTO;
import com.example.demo.dto.GroupDTO;
import com.example.demo.dto.ParentWithChildrenDTO;
import com.example.demo.exception.GroupNotFoundException;
import com.example.demo.repository.GroupQueryRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.users.entity.Member;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final MemberRepository memberRepository;
    private final GroupQueryRepository groupQueryRepository;
    private final GroupQueryRepository managerQueryRepository;
    public GroupDTO getGroup(Long groupId) throws GroupNotFoundException {

        Optional<GroupDTO> groupDTO = groupQueryRepository.getGroupWithChildrenByGroupId(groupId);

        if(groupDTO.isEmpty()){
            throw new GroupNotFoundException();
        }

        return groupDTO.get();
    }

    public Page<ParentWithChildrenDTO> getParentCardsByGroup(Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Member> parentPage = memberRepository.findDistinctByChildren_Group_Id(groupId, pageable);

        List<ParentWithChildrenDTO> cardList = parentPage.stream().map(member -> {
            // ⭐️ 여기서 groupId 기준으로 자녀 필터링!
            List<ChildSimpleDTO> filteredChildren = member.getChildren().stream()
                    .filter(child -> child.getGroup() != null && child.getGroup().getId().equals(groupId))
                    .map(child -> ChildSimpleDTO.builder()
                            .childId(child.getId())
                            .childName(child.getName())
                            .gender(child.getGender())
                            .birthday(String.valueOf(child.getBirthday()))
                            .nickname(child.getNickname())
                            .height(child.getHeight())
                            .weight(child.getWeight())
                            .birthOrder(child.getBirthOrder())
                            .riskGroup(child.getRiskGroup() != null ? child.getRiskGroup() : null)
                            .build())
                    .collect(Collectors.toList());

            return ParentWithChildrenDTO.builder()
                    .parentId(member.getId())
                    .parentName(member.getName())
                    .parentPhone(member.getPhoneNumber())
                    .children(filteredChildren)
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(cardList, pageable, parentPage.getTotalElements());
    }



}
