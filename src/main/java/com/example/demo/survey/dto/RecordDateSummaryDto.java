package com.example.demo.survey.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

/**
 * 특정 자녀의 기록 문진 이력에서 문진이 진행된 날짜들을 요약하여 보여줄 때 사용되는 DTO입니다.
 * 주로 관리자 페이지에서 자녀별 문진 기록 날짜 목록을 페이징하여 표시하는 데 활용됩니다.
 * 이 DTO는 개별 답변 내용을 포함하지 않고, "어떤 날짜에 문진이 기록되었다"는 요약 정보만을 제공합니다.
 */
@Getter
@Builder // Builder 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
public class RecordDateSummaryDto {
    /**
     * 문진이 기록된 특정 날짜입니다. (연, 월, 일 정보만 포함)
     */
    private LocalDate recordDate;
    /**
     * 이 문진 기록 날짜가 속한 자녀의 고유 ID입니다.
     * 클라이언트 측에서 특정 자녀의 날짜별 기록을 조회할 때 식별자로 사용될 수 있습니다.
     */
    private Long childId;
}