package com.gtkim.mobile_access_control.component.auth.data.remote

import com.gtkim.mobile_access_control.component.auth.data.remote.dto.LoginRequest
import com.gtkim.mobile_access_control.component.auth.data.remote.dto.LoginResponse
import com.gtkim.mobile_access_control.component.auth.data.remote.dto.RefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    /** Refresh Token 은 [authorization] 헤더(`Bearer <refresh>`)로 전송. 본문 없음. */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Header("Authorization") authorization: String,
    ): RefreshResponse

    /**
     * 로그아웃 — 서버 측 Refresh Token 무효화. 응답 204 No Content.
     *
     * [authorization] 은 Access OR Refresh Token (`Bearer <any>`). 서버가 종류를 자동 감지한다.
     * 비-2xx 도 예외 없이 [Response] 로 받아 best-effort 처리한다 (§3.3).
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String,
    ): Response<Unit>
}
