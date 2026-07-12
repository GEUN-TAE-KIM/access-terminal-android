package com.gtkim.mobile_access_control.component.master.data.remote

import com.gtkim.mobile_access_control.component.master.data.remote.dto.MasterSnapshotResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

internal interface MasterDataApi {

    /**
     * `GET /api/v1/master/snapshot` (api-spec §8.1).
     *
     * RFC 7232 Conditional Request 패턴 — 단말의 마지막 ETag 를 `If-None-Match` 로 echo 하면
     * 서버가 변경 없을 때 304 Not Modified 를 본문 없이 반환한다. Retrofit 의 [Response] 로
     * 받아 `code() == 304` 분기 + `headers().get("ETag")` 추출.
     *
     * 첫 sync 또는 캐시 손상 시 [eTag] null 로 호출 → 서버가 full snapshot 반환.
     */
    @GET("api/v1/master/snapshot")
    suspend fun snapshot(
        @Header("If-None-Match") eTag: String? = null,
    ): Response<MasterSnapshotResponse>
}
