package com.example.demo.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Date;
import java.time.LocalDate;

/**
 * 특정 자녀의 기록 문진 이력에서 문진이 진행된 날짜들을 요약하여 전달하는 DTO.
 * <p>
 * 사용 예:
 * - 관리자 페이지에서 자녀별 문진 기록 날짜를 페이징 방식으로 조회할 때 활용
 * - 개별 문진 내용은 포함하지 않으며, 날짜 및 자녀 식별자 정보만 포함
 */
@Getter
@Builder
@AllArgsConstructor
public class RecordDateSummaryDto {

    /**
     * 문진이 기록된 날짜 (연-월-일).
     */
    private LocalDate recordDate;

    /**
     * 해당 날짜에 문진을 작성한 자녀의 ID.
     */
    private Long childId;

    /**
     * SQL 결과에서 java.sql.Date를 변환하여 생성할 수 있도록 하는 생성자.
     *
     * @param sqlDate  java.sql.Date 타입 날짜
     * @param childId  자녀 ID
     */
    public RecordDateSummaryDto(Date sqlDate, Long childId) {
        this.recordDate = sqlDate != null ? sqlDate.toLocalDate() : null;
        this.childId = childId;
    }
}
