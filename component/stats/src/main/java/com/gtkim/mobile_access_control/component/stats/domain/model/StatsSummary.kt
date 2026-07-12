package com.gtkim.mobile_access_control.component.stats.domain.model

/**
 * 일별 통계 요약 (API 명세 §6.1 응답 `summary`).
 */
data class StatsSummary(
    val totalAttempts: Int,
    val allowed: Int,
    val denied: Int,
    /**
     * 허용률 (0.0 ~ 1.0). [totalAttempts] 가 0 이면 `null` — division-by-zero 회피.
     * 클라이언트는 `null` 일 때 "데이터 없음" UI 로 처리한다 (API 명세 §6.1).
     */
    val allowedRate: Double?,
)
