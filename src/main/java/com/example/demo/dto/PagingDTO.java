package com.example.demo.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
public class PagingDTO<T> {
    private Integer size;
    private Integer page;
    private String sort; // Manager에 있는 users.name을 기준으로 정렬하는 경우 "users.name"
    private String direction;

    private List<T> data;

    /**
     * 기본값을 고려해서 Pageable 객체 반환
     */
    public Pageable toPageable() {
        int safeSize = (size != null && size > 0) ? size : 10;
        int safePage = (page != null && page >= 0) ? page -1 : 0; // 페이지 번호 1번부터 보여주고 싶어서 -1
        String safeSort = (sort != null && !sort.isBlank()) ? sort : "createdAt";
        Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSort));
    }
}
