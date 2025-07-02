//package com.example.demo.survey.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//
//import java.sql.Date;
//import java.time.LocalDate;
//
///**
// * 특정 자녀의 기록 문진 이력에서 문진이 진행된 날짜들을 요약하여 전달하는 DTO.
// * <p>
// * 사용 예:
// * - 관리자 페이지에서 자녀별 문진 기록 날짜를 페이징 방식으로 조회할 때 활용
// * - 개별 문진 내용은 포함하지 않으며, 날짜 및 자녀 식별자 정보만 포함
// */
//@Getter
//@Builder
//@AllArgsConstructor
//public class RecordDateSummaryDto {
//
//    /**
//     * 문진이 기록된 날짜 (연-월-일).
//     */
//    private LocalDate recordDate;
//
//    /**
//     * 해당 날짜에 문진을 작성한 자녀의 ID.
//     */
//    private Long childId;
//
//    /**
//     * SQL 결과에서 java.sql.Date를 변환하여 생성할 수 있도록 하는 생성자.
//     *
//     * @param sqlDate  java.sql.Date 타입 날짜
//     * @param childId  자녀 ID
//     */
//    public RecordDateSummaryDto(Date sqlDate, Long childId) {
//        this.recordDate = sqlDate != null ? sqlDate.toLocalDate() : null;
//        this.childId = childId;
//    }
//}
package com.example.demo.survey.dto;

import lombok.Getter;

import java.sql.Date;
import java.time.LocalDate;

@Getter
public class SpecialDateSummaryDto {

    private LocalDate recordDate;
    private Long childId;

    /**
     * ✨ [문제 해결] 디버깅 결과, JPA는 이 생성자를 사용하고 있었습니다! ✨
     * @param recordDate DB에서 반환된 java.sql.Date 타입
     * @param childId DB에서 반환된 Long 타입
     */
    public SpecialDateSummaryDto(Date recordDate, Long childId) {
        // 일관성 있는 타입 관리를 위해 내부적으로는 LocalDate로 변환하여 사용
        this.recordDate = (recordDate != null) ? recordDate.toLocalDate() : null;
        this.childId = childId;
    }

    // 다른 환경(DB)에서는 LocalDate를 직접 반환할 수도 있으므로, 이 생성자도 함께 두는 것이 안정적입니다.
    public SpecialDateSummaryDto(LocalDate recordDate, Long childId) {
        this.recordDate = recordDate;
        this.childId = childId;
    }

    // 기본 생성자가 필요한 다른 로직이 있다면 유지합니다.
    public SpecialDateSummaryDto() {}
}