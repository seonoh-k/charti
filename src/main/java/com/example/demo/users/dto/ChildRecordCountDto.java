package com.example.demo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code ChildRecordCountDto}
 *
 * 특정 자녀의 기록 문진 작성 횟수를 나타내는 DTO입니다.
 * 주로 자녀별 문진 이력 요약 통계를 계산하거나 표시할 때 사용됩니다.
 *
 * <p><b>필드 설명:</b></p>
 * <ul>
 *     <li>{@code childId} - 자녀의 고유 ID</li>
 *     <li>{@code recordCount} - 해당 자녀가 작성한 기록 문진 횟수</li>
 * </ul>
 *
 * 이 DTO는 조회 결과를 집계할 때 주로 Repository에서 JPQL/SQL 결과를 매핑하는 데 사용됩니다.
 */
@Getter
@AllArgsConstructor
public class ChildRecordCountDto {
    private Long childId;
    private Long recordCount;
}
