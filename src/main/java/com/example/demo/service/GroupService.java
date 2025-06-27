package com.example.demo.service;

import com.example.demo.dto.ChildSimpleDTO;
import com.example.demo.dto.ChildWithParentDTO;
import com.example.demo.dto.GroupDTO;
import com.example.demo.dto.ParentWithChildrenDTO;
import com.example.demo.exception.GroupNotFoundException;
import com.example.demo.repository.GroupQueryRepository;
import com.example.demo.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupQueryRepository groupQueryRepository;
    private final GroupQueryRepository managerQueryRepository;
    public GroupDTO getGroup(Long groupId) throws GroupNotFoundException {

        Optional<GroupDTO> groupDTO = groupQueryRepository.getGroupWithChildrenByGroupId(groupId);

        if(groupDTO.isEmpty()){
            throw new GroupNotFoundException();
        }

        return groupDTO.get();
    }

    public List<ParentWithChildrenDTO> getChildrenWithParentByGroupId(Long groupId) {
        List<ChildWithParentDTO> all = managerQueryRepository.findChildrenWithParentByGroupId(groupId);

        // [2] 부모별로 그룹핑(카드형)
        Map<Long, ParentWithChildrenDTO> parentMap = new LinkedHashMap<>();
        for (ChildWithParentDTO dto : all) {
            Long parentId = dto.getParentId();
            ParentWithChildrenDTO parentCard = parentMap.get(parentId);
            if (parentCard == null) {
                parentCard = ParentWithChildrenDTO.builder()
                        .parentId(parentId)
                        .parentName(dto.getParentName())
                        .parentPhone(dto.getParentPhone())
                        .children(new ArrayList<>())
                        .build();
                parentMap.put(parentId, parentCard);
            }
            parentCard.getChildren().add(
                    ChildSimpleDTO.builder()
                            .childId(dto.getChildId())
                            .childName(dto.getChildName())
                            .gender(dto.getGender())
                            .birthday(dto.getBirthday())
                            .nickname(dto.getNickname())
                            .height(dto.getHeight())
                            .weight(dto.getWeight())
                            .birthOrder(dto.getBirthOrder())
                            .riskGroup(dto.getRiskGroup())
                            .build()
            );
        }
        return new ArrayList<>(parentMap.values());
    }
}
