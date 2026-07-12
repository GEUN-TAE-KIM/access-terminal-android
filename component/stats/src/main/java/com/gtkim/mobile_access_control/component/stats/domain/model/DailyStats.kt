package com.gtkim.mobile_access_control.component.stats.domain.model

import java.time.LocalDate

/**
 * 일별 출입 통계 (API 명세 §6.1 응답).
 */
data class DailyStats(
    val date: LocalDate,
    val summary: StatsSummary,
    /** 0~23시 전부 포함 (count 0 도 명시). */
    val byHour: List<HourlyCount>,
    /** 거부 사유별 집계, count 내림차순. 거부 0건이면 빈 리스트. */
    val byDenyReason: List<DenyReasonCount>,
)
