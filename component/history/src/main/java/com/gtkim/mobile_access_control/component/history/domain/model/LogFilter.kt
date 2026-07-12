package com.gtkim.mobile_access_control.component.history.domain.model

import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.DenyReason
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import java.time.Instant

/**
 * 출입 기록 조회 필터 (API 명세 §5.1 쿼리 파라미터).
 *
 * 모든 필터는 서버 측에서 적용된다 — cursor pagination 일관성을 위해 클라 측 필터링 금지
 * (architecture.md §2).
 *
 * [from]/[to] 는 절대 시각([Instant]) 으로, 전송 시 ISO 8601 UTC datetime 으로 직렬화된다.
 * 명세의 `tz` 파라미터는 date-only 입력 해석용이므로 사용하지 않는다 (클라는 항상 datetime 전송).
 */
data class LogFilter(
    /** 페이지 크기 (1~100). */
    val size: Int = 20,
    val from: Instant? = null,
    val to: Instant? = null,
    val result: LogResultFilter = LogResultFilter.ALL,
    /** 특정 거부 사유. wire 직렬화는 [DenyReason.toWire] 가 담당. */
    val denyReason: DenyReason? = null,
    val employeeCode: EmployeeCode? = null,
    val cardUid: CardUid? = null,
)
