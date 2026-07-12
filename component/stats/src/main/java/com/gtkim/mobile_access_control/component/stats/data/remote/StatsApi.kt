package com.gtkim.mobile_access_control.component.stats.data.remote

import com.gtkim.mobile_access_control.component.stats.data.remote.dto.DailyStatsResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface StatsApi {

    /**
     * 일별 출입 통계 — `GET /api/v1/access/stats` (API 명세 §6.1).
     *
     * `tz` 는 생략한다 — 서버 기본값(Asia/Tokyo)이 본 앱 타깃 타임존과 일치한다 (architecture.md §7).
     * `date` 는 `YYYY-MM-DD` (`LocalDate.toString()`).
     */
    @GET("api/v1/access/stats")
    suspend fun stats(@Query("date") date: String): DailyStatsResponse
}
