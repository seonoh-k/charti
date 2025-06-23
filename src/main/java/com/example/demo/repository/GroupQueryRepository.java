package com.example.demo.repository;

import com.example.demo.dto.GroupDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface GroupQueryRepository {

    // GroupId로 연결된 ChildList까지 get
    Optional<GroupDTO> getGroupWithChildrenByGroupId(Long groupId);

}
