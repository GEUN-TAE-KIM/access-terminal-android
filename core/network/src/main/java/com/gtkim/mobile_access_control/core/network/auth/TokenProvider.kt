package com.gtkim.mobile_access_control.core.network.auth

import java.time.Instant

/**
 * [com.gtkim.mobile_access_control.core.network.interceptor.AuthInterceptor] 가 토큰에
 * 접근하기 위한 seam(이음새).
 *
 * 구현은 `:component:auth` 가 제공한다 — `:core:network` 는 토큰 저장 방식·refresh API 를 모른다.
 */
interface TokenProvider {

    /**
     * 현재 Access Token 스냅샷. 매 HTTP 요청 핫패스에서 호출되므로 non-suspend —
     * 구현체는 메모리 캐시에서 즉시 반환해야 한다. 미로그인이면 `null`.
     */
    fun currentAccessToken(): String?

    /**
     * 현재 Access Token 만료 시각. `null` = 만료 정보 모름 (이전 버전 저장 데이터 호환).
     *
     * 인터셉터가 401 을 받기 전에 사전 갱신을 판단하기 위해 사용한다 (API 명세 §3.1
     * "만료 1분 전 자동 갱신 권장").
     */
    fun accessTokenExpiresAt(): Instant?

    /**
     * Refresh Token 으로 Access/Refresh Token 을 재발급하고 저장한다. 성공 시 `true`.
     *
     * single-flight(동시 401 중 1회만 호출) 보장은 호출자([AuthInterceptor])의 책임이다.
     */
    suspend fun refreshTokens(): Boolean
}
