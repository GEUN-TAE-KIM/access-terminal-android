package com.gtkim.mobile_access_control.core.network.interceptor

import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.network.auth.TokenProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.time.Duration

/**
 * 메인 client 의 모든 요청에 `Authorization: Bearer <access>` 를 부착하고,
 * 만료 임박 시 사전 갱신, `401` 응답 시 사후 갱신 후 재시도를 처리한다.
 *
 * **사전 갱신 (proactive refresh)** — 응답 `expiresIn`(초) 기반으로 저장된 만료 시각이
 * [PROACTIVE_REFRESH_LEAD] 이내로 임박하면 요청을 보내기 전에 먼저 single-flight refresh.
 * 401 race 가 빈번하면 Refresh Token Rotation 으로 세션이 흔들리므로 가능한 한 401 자체를 피한다
 * (API 명세 §3.1 "만료 1분 전 자동 갱신 권장", architecture.md §2 "expiresIn 우선 사용").
 *
 * **single-flight refresh** — 여러 요청이 동시에 만료된 토큰으로 출발해 N 개의 401 을
 * 동시에 받으면, 각자 refresh 를 호출할 경우 Refresh Token Rotation 정책상 두 번째 이후가
 * 모두 무효화되어 사용자가 강제 로그아웃된다. [refreshMutex] 로 첫 요청만 실제 refresh 를
 * 수행하고 나머지는 그 결과(새 토큰)를 재사용한다.
 *
 * **재귀 차단** — `AuthApi`(login/refresh)는 이 인터셉터가 빠진 전용 client(`@AuthApiClient`)
 * 를 쓰므로 refresh 호출이 이 인터셉터를 다시 타지 않는다.
 *
 * **runBlocking** — OkHttp `intercept()` 는 동기 API 라 suspend 인 [TokenProvider.refreshTokens]
 * 를 `runBlocking` 으로 래핑한다. OkHttp dispatcher 스레드 위에서 도므로 메인 스레드는 막지 않는다.
 */
class AuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val time: TimeProvider,
) : Interceptor {

    private val refreshMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val initialToken = tokenProvider.currentAccessToken()

        val tokenToSend = if (initialToken != null && isAccessTokenExpiringSoon()) {
            runBlocking { refreshSingleFlight(initialToken) } ?: initialToken
        } else initialToken

        val response = chain.proceed(original.withBearer(tokenToSend))
        if (response.code != HTTP_UNAUTHORIZED) return response

        // 401 — Refresh Token 으로 갱신 시도. 실패하면 원본 401 을 그대로 반환.
        val newToken = runBlocking { refreshSingleFlight(tokenToSend) } ?: return response

        response.close()
        return chain.proceed(original.withBearer(newToken))
    }

    /**
     * 락을 잡은 첫 요청만 실제 refresh 를 수행한다. 락 대기 중 다른 요청이 이미 갱신했으면
     * (= 현재 토큰이 내가 보낸 토큰과 다르면) 그 토큰을 재사용해 중복 refresh 를 차단한다.
     */
    private suspend fun refreshSingleFlight(sentToken: String?): String? = refreshMutex.withLock {
        val current = tokenProvider.currentAccessToken()
        if (current != null && current != sentToken) return current
        if (tokenProvider.refreshTokens()) tokenProvider.currentAccessToken() else null
    }

    /** 만료 시각 정보가 없으면(이전 버전 저장 데이터) 사전 갱신 안 함 — 401 사후 갱신에 의존. */
    private fun isAccessTokenExpiringSoon(): Boolean {
        val expiresAt = tokenProvider.accessTokenExpiresAt() ?: return false
        return time.now().plus(PROACTIVE_REFRESH_LEAD).isAfter(expiresAt)
    }

    private fun Request.withBearer(token: String?): Request =
        if (token == null) this
        else newBuilder().header(HEADER_AUTHORIZATION, "Bearer $token").build()

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HEADER_AUTHORIZATION = "Authorization"
        val PROACTIVE_REFRESH_LEAD: Duration = Duration.ofSeconds(60)
    }
}
