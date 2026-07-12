package com.gtkim.mobile_access_control.component.history.domain.model

/**
 * 로그 조회 시 `result` 쿼리 필터 (API 명세 §5.1).
 *
 * 응답의 [LogResult] (6종 구체값)와 달리, 필터는 `DENIED` 가 모든 거부 그룹을 포함하는
 * 3값이다. 더 세분화된 거부 필터는 [LogFilter.denyReason] 으로 지정한다.
 */
enum class LogResultFilter {
    ALL,
    ALLOWED,
    DENIED,
}
