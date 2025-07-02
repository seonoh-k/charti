package com.example.demo.dto.paging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {
 * "name": "SUCCESS_WITH_DATA",
 * "code": "SD",
 * "message": "데이터를 정상적으로 반환하였습니다",
 * "data":{ "result":[
 *                      {"id": 235, "name": "박은정", "email": "test150@example.com", "phone": "010-9812-6027"},
 *                      {"id": 234, "name": "이상호", "email": "test149@example.com", "phone": "010-6578-6291"},
 *                      {"id": 233, "name": "김서준", "email": "test148@example.com", "phone": "010-4043-9903"},
 *                      {"id": 232, "name": "강민지", "email": "test147@example.com", "phone": "010-6763-6003"},
 *                      {"id": 231, "name": "한준서", "email": "test146@example.com", "phone": "010-6250-8486"},
 *                      {"id": 230, "name": "김정남", "email": "test145@example.com", "phone": "010-3220-4231"},
 *                      {"id": 229, "name": "손지연", "email": "test144@example.com", "phone": "010-1879-7967"},
 *                      {"id": 228, "name": "홍미숙", "email": "test143@example.com", "phone": "010-7240-9174"},
 *                      {"id": 227, "name": "김정희", "email": "test142@example.com", "phone": "010-4130-7293"},
 *                      {"id": 226, "name": "이서준", "email": "test141@example.com", "phone": "010-3775-3273"}]
 *          ,
 *          "page": 1,
 *          "size": 10,
 *          "totalPages": 16,
 *          "totalElements": 154,
 *          "hasPrev": false,
 *          "hasNext": true,
 *          "pageList":[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
 * },
 * "fieldName": null
 * }
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagingResponse<T> {

    private List<T> result;              // 실제 데이터.객체
    private Integer page;               // 현재 페이지
    private Integer size;               // 한번에 보여줄 요소
    private Integer totalPages;         // 전체 페이지의 개수
    private Long totalElements;         // 전체 요소의 개수
    private boolean hasPrev;
    private boolean hasNext;
    private List<Integer> pageList;     // 1~9 페이지 개수

    public static <T> PagingResponse<T> from(PagingResultDTO<T, ?> result) {
        return new PagingResponse<>(
                result.getDtoList(),
                result.getPage(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements(),
                result.isHasPrev(),
                result.isHasNext(),
                result.getPageList()
        );
    }

}
