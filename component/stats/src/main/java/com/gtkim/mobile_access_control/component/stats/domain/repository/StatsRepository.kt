package com.gtkim.mobile_access_control.component.stats.domain.repository

import com.gtkim.mobile_access_control.component.stats.domain.model.DailyStats
import com.gtkim.mobile_access_control.component.stats.domain.model.StatsError
import com.gtkim.mobile_access_control.core.common.result.Outcome
import java.time.LocalDate

interface StatsRepository {

    /**
     * 지정한 날짜의 일별 출입 통계를 조회한다 — `GET /api/v1/access/stats`.
     *
     * [date] 는 달력상의 날짜이며, 서버는 Asia/Tokyo 자정~자정 범위로 해석한다 (API 명세 §6.1).
     */
    suspend fun daily(date: LocalDate): Outcome<DailyStats, StatsError>
}
