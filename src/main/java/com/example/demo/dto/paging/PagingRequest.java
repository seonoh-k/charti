package com.example.demo.dto.paging;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagingRequest {

    private Integer size;
    private Integer page;
    private String sort; // Manager에 있는 users.name을 기준으로 정렬하는 경우 "users.name"
    private String direction;

    /**
     * 기본값을 고려해서 Pageable 객체 반환
     * pageable은 0부터 시작하니까 사용자에게 기존에는 1 높여서 보여줌
     * 실제 벡엔드 서버에서 값 가공은 -1로 처리한 다음 진행해야함
     */
    public Pageable toPageable() {
        int safeSize = (size != null && size > 0) ? size : 10;
        int safePage = (page != null && page > 1) ? page - 1 : 0; // 페이지 번호 1번부터 보여주고 싶어서 -1

        String safeSort = (sort != null && !sort.isBlank()) ? sort : "createdAt";
        Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSort));
    }
    public Pageable toZeroBasedPageable() {
        int safeSize = (size != null && size > 0) ? size : 10;
        int safePage = (page != null && page >= 0) ? page : 0;
        String safeSort = (sort != null && !sort.isBlank()) ? sort : "createdAt";
        Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSort));
    }

}

