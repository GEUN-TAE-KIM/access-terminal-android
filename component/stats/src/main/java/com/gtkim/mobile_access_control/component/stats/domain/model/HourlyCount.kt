package com.gtkim.mobile_access_control.component.stats.domain.model

/**
 * 시간대별 출입 집계 (API 명세 §6.1 응답 `byHour[]`).
 *
 * 서버가 0~23시를 모두 채워 보내므로 (count 0 도 명시) 클라이언트는 빈 시간대를 채울 필요가 없다.
 */
data class HourlyCount(
    val hour: Int,
    val allowed: Int,
    val denied: Int,
)
