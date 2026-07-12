package com.gtkim.mobile_access_control.component.auth.data.local

import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

/**
 * 인증 토큰 영속 저장소.
 * 구현체는 DataStore Preferences + AndroidKeyStore AES-GCM 으로 암호화 (DataStoreTokenStorage).
 *
 * 설계 노트:
 *   read 경로는 **non-suspend**. 인터셉터처럼 매 HTTP 요청마다 호출되는 핫패스에서
 *   Flow collector 할당이나 runBlocking 을 피하기 위해, 구현체는 StateFlow 캐시를
 *   유지하고 .value 즉시 반환만 한다. 디스크 1회 로드는 [warmUp] 으로 선반영.
 *
 *   write 경로는 suspend — 실제 디스크 쓰기 + 직렬화 + 암호화가 동반되므로.
 */
internal interface TokenStorage {

    /**
     * 토큰 상태 스트림. UI/관찰자가 토큰 변화에 반응해야 할 때 구독.
     * 인터셉터/리포지토리는 [accessToken]/[refreshToken] 로 스냅샷만 읽으면 충분.
     */
    val tokens: StateFlow<AuthTokens>

    fun accessToken(): String?
    fun refreshToken(): String?

    /** Access Token 만료 시각. `null` = 만료 정보 모름 (이전 버전 저장 데이터 호환). */
    fun accessTokenExpiresAt(): Instant?

    /**
     * StateFlow 캐시에 첫 디스크 값이 반영될 때까지 대기.
     * 콜드스타트 직후 첫 HTTP 요청이 EMPTY 토큰으로 나가는 race 를 막기 위해
     * 부트스트랩 시점에 1회 호출한다.
     */
    suspend fun warmUp()

    suspend fun save(
        accessToken: String,
        refreshToken: String,
        accessExpiresAt: Instant,
        refreshExpiresAt: Instant,
    )

    suspend fun clear()
}
