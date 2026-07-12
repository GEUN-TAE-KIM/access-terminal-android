package com.gtkim.mobile_access_control.component.auth.data.local

import kotlinx.serialization.Serializable

/**
 * DataStore 에 저장되는 토큰 묶음.
 *
 * Refresh Token Rotation (§2) 정책상 access + refresh 는 항상 묶음으로 갱신·삭제.
 * `null` = 미로그인 상태. 빈 문자열과 의미 구분 명확화.
 *
 * 만료 시각은 응답의 `expiresIn`(초) 기반으로 `TimeProvider.now() + expiresIn` 으로 계산해
 * epoch millis 로 저장한다 — 절대 시각(`expiresAt`)을 그대로 받으면 단말 시계가 서버와
 * 어긋난 환경에서 사전 갱신 판정이 틀어지므로 (API 명세 §3.1, architecture.md §2).
 * `null` = 만료 정보 없음(이전 버전 저장 데이터 호환).
 */
@Serializable
internal data class AuthTokens(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val accessExpiresAtEpochMs: Long? = null,
    val refreshExpiresAtEpochMs: Long? = null,
) {
    companion object {
        val EMPTY = AuthTokens()
    }
}
