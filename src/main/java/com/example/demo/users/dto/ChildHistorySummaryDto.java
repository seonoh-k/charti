package com.example.demo.users.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 특정 부모(Member)에게 연결된 자녀들의 문진 기록 요약 정보를 담는 DTO입니다.
 * 주로 관리자 페이지에서 부모 선택 후 해당 부모의 자녀 목록을 표시할 때 사용됩니다.
 * 각 자녀의 이름, 나이, 연령대, 그리고 해당 자녀가 문진을 기록한 총 날짜 수를 요약하여 제공합니다.
 */
@Getter
@Builder // Builder 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
public class ChildHistorySummaryDto {
    /**
     * 자녀의 고유 ID입니다.
     */
    private Long childId;
    /**
     * 자녀의 이름입니다.
     */
    private String childName;
    /**
     * 자녀의 현재 나이입니다.
     */
    private int childAge;
    /**
     * 이 자녀가 문진 기록을 진행한 총 날짜 수입니다.
     * 이는 해당 자녀가 작성한 문진의 고유한 날짜(recordDate)의 총 개수를 의미합니다.
     */
    private long totalRecordDatesCount;
}