package com.example.demo.dto.paging;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;



/**
 * 요소가 154개 있는 경우
 *   dtoList;
 *   page; 현재 페이지 -> 1
 *   size; 페이지 당 보여줄 요소의 수 -> 10
 *   totalPages; 전체 페이지 개수 -> 16
 *   totalElements; 요소 전체의 개수 -> 154
 *   hasPrev; ->
 *   hasNext;
 *   pageList;
 *
 *   data.dtoList로 접근
 *   data.pageList로 접근
 * @param <DTO>
 * @param <EN>
 */
@Data
public class PagingResultDTO<DTO, EN> {


    private List<DTO> dtoList;
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalElements;
    private boolean hasPrev;
    private boolean hasNext;
    private List<Integer> pageList;
    private Integer startPage;
    private Integer endPage;

    public PagingResultDTO(Page<EN> result, Function<EN, DTO> fn) {
        this.dtoList = result.stream().map(fn).toList();
        this.totalPages = result.getTotalPages();
        this.totalElements = result.getTotalElements();
        this.page = result.getNumber() + 1;
        this.size = result.getSize();
        this.hasPrev = result.hasPrevious();
        this.hasNext = result.hasNext();

        makePageList();
    }

    private void makePageList() {
        // 현재 페이지가 3인경우 임시 마지막 페이지는 10임
        int tempEnd = (int)(Math.ceil(page / 10.0)) * 10;
        this.startPage = Math.max(tempEnd - 9, 1);
        this.endPage= Math.min(totalPages, tempEnd);
        this.pageList = IntStream.rangeClosed(startPage, endPage).boxed().toList();
    }
}
