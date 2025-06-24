package com.example.demo.users.repository;

import com.example.demo.dto.ChildDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChildQueryRepository {

    Page<ChildDTO> getChildDTOsByGroupIdOrderByAgeAndName(Long groupId,Pageable pageable);
}
