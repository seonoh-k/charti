package com.example.demo.service;

import com.example.demo.dto.GroupDTO;
import com.example.demo.exception.GroupNotFoundException;
import com.example.demo.repository.GroupQueryRepository;
import com.example.demo.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupQueryRepository groupQueryRepository;

    public GroupDTO get(Long groupId) throws GroupNotFoundException {

        Optional<GroupDTO> groupDTO = groupQueryRepository.getGroupWithChildrenByGroupId(groupId);

        if(groupDTO.isEmpty()){
            throw new GroupNotFoundException();
        }

        return groupDTO.get();
    }
}
