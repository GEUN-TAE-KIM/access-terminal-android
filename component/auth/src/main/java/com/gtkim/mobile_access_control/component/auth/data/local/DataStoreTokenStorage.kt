package com.gtkim.mobile_access_control.component.auth.data.local

import androidx.datastore.core.DataStore
import com.gtkim.mobile_access_control.core.di.qualifier.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Typed DataStore<AuthTokens> 기반 토큰 저장소.
 *
 * 직렬화·암호화는 `AuthTokensSerializer` 가 담당. 본 클래스는 변환에 관여하지 않음.
 *
 * 퍼포먼스 설계:
 *   read 핫패스(매 HTTP 요청 인터셉터에서 호출) 비용을 메모리 lookup 으로 줄이기 위해
 *   `store.data` 를 [stateIn] 으로 단일 [StateFlow] 캐시로 승격시킨다.
 *
 *     - 업스트림 수집: [AppScope] 위에서 [SharingStarted.Eagerly] — 앱이 살아있는 동안 1회
 *     - 첫 emission: 디스크 read + 복호화 + 역직렬화 (1회, [warmUp] 으로 선반영)
 *     - 이후 read: [StateFlow.value] 의 atomic load — 할당 0, suspend 0
 *
 *   write 는 [DataStore.updateData] 가 직렬화/암호화/원자적 쓰기를 담당하고, 그 결과가
 *   업스트림 Flow 로 흘러 자동으로 캐시에 반영된다 (별도 갱신 코드 불필요).
 */
@Singleton
internal class DataStoreTokenStorage @Inject constructor(
    private val store: DataStore<AuthTokens>,
    @AppScope appScope: CoroutineScope,
) : TokenStorage {

    private val warmed = AtomicBoolean(false)

    override val tokens: StateFlow<AuthTokens> = store.data
        .onEach { warmed.set(true) }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = AuthTokens.EMPTY,
        )

    override fun accessToken(): String? = tokens.value.accessToken

    override fun refreshToken(): String? = tokens.value.refreshToken

    override fun accessTokenExpiresAt(): Instant? =
        tokens.value.accessExpiresAtEpochMs?.let(Instant::ofEpochMilli)

    override suspend fun warmUp() {
        if (warmed.get()) return
        // StateFlow 의 첫 값은 initialValue(EMPTY) 라 그대로 받으면 안 됨.
        // onEach 가 실제 디스크 emission 시 warmed=true 로 토글한 뒤에 통과시킨다.
        tokens.filter { warmed.get() }.first()
    }

    override suspend fun save(
        accessToken: String,
        refreshToken: String,
        accessExpiresAt: Instant,
        refreshExpiresAt: Instant,
    ) {
        store.updateData {
            AuthTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
                accessExpiresAtEpochMs = accessExpiresAt.toEpochMilli(),
                refreshExpiresAtEpochMs = refreshExpiresAt.toEpochMilli(),
            )
        }
    }

    override suspend fun clear() {
        store.updateData { AuthTokens.EMPTY }
    }
}
