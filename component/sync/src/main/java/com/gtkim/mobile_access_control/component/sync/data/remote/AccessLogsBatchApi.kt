package com.gtkim.mobile_access_control.component.sync.data.remote

import com.gtkim.mobile_access_control.component.sync.data.remote.dto.AccessLogsBatchRequest
import com.gtkim.mobile_access_control.component.sync.data.remote.dto.AccessLogsBatchResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 오프라인 audit 일괄 업로드 — `POST /api/v1/access/logs/batch` (api-spec §5.2).
 *
 * 멱등성은 각 item 의 `clientLogId` 가 보장. 같은 키 재전송은 서버가 dedup → `duplicates` 카운트만
 * 올라가고 row 는 1개. 부분 성공도 200 으로 응답 — `accepted`/`duplicates`/`rejected[]` 로 분류된다.
 */
internal interface AccessLogsBatchApi {
    @POST("api/v1/access/logs/batch")
    suspend fun upload(@Body req: AccessLogsBatchRequest): AccessLogsBatchResponse
}
