package com.gtkim.mobile_access_control.component.history.data.remote

import com.gtkim.mobile_access_control.component.history.data.remote.dto.LogPageResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface HistoryApi {

    /**
     * 출입 기록 조회 — `GET /api/v1/access/logs` (API 명세 §5.1). Cursor 기반 페이지네이션.
     *
     * `null` 인 쿼리 파라미터는 Retrofit 이 자동 생략한다. [cursor] 는 서버가 준 값을 그대로
     * 다시 넘긴다 — 클라는 디코딩·재구성하지 않는다 (architecture.md §2).
     */
    @GET("api/v1/access/logs")
    suspend fun logs(
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("result") result: String? = null,
        @Query("denyReason") denyReason: String? = null,
        @Query("employeeCode") employeeCode: String? = null,
        @Query("cardUid") cardUid: String? = null,
    ): LogPageResponse
}
