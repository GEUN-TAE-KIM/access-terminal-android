package com.gtkim.mobile_access_control.component.auth.data

import com.gtkim.mobile_access_control.component.auth.data.error.toAuthError
import com.gtkim.mobile_access_control.component.auth.data.local.TokenStorage
import com.gtkim.mobile_access_control.component.auth.data.mapper.toDomain
import com.gtkim.mobile_access_control.component.auth.data.remote.AuthApi
import com.gtkim.mobile_access_control.component.auth.data.remote.dto.LoginRequest
import com.gtkim.mobile_access_control.component.auth.domain.model.Admin
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthError
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthState
import com.gtkim.mobile_access_control.component.auth.domain.repository.AuthRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.result.safeCall
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.di.qualifier.AppScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

internal class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val storage: TokenStorage,
    private val time: TimeProvider,
    @param:AppScope private val appScope: CoroutineScope,
) : AuthRepository {

    /**
     * [TokenStorage.tokens] 를 도메인 [AuthState] 로 매핑한 핫 스트림.
     *
     * [SharingStarted.Eagerly] — Navigation 이 collect 하기 전이라도 인터셉터가 토큰을 폐기하는
     * 즉시 LoggedOut 으로 전이되어 있도록. 초기값은 LoggedOut (storage warmUp 전 EMPTY 와 일치).
     */
    override val authState: StateFlow<AuthState> = storage.tokens
        .map { if (it.accessToken != null) AuthState.LoggedIn else AuthState.LoggedOut }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.LoggedOut,
        )

    override suspend fun login(loginId: String, password: String): Outcome<Admin, AuthError> =
        safeCall(Throwable::toAuthError) {
            val res = api.login(LoginRequest(username = loginId, password = password))
            val now = time.now()
            storage.save(
                accessToken = res.accessToken,
                refreshToken = res.refreshToken,
                accessExpiresAt = now.plus(Duration.ofSeconds(res.accessTokenExpiresIn)),
                refreshExpiresAt = now.plus(Duration.ofSeconds(res.refreshTokenExpiresIn)),
            )
            res.admin.toDomain()
        }

    /**
     * 로그아웃 — best-effort (§3.3).
     *
     * 로컬 토큰을 **먼저** 폐기해 [authState] 를 즉시 LoggedOut 으로 전이시킨다 (UI 즉시 반응).
     * 서버 호출은 [appScope] 의 fire-and-forget — 오프라인이거나 서버가 죽어 있어도 UI 가
     * 타임아웃까지 멈추지 않는다. 서버 측 Refresh Token 잔존 가능성은 지수 백오프 1회 재시도
     * (백그라운드) 후 포기.
     *
     * Authorization 헤더는 Refresh Token 우선 — Access(30분)는 만료됐을 수 있고 서버가
     * 어느 토큰이든 자동 감지하므로 더 오래 사는 Refresh 를 보낸다.
     */
    override suspend fun logout() {
        val token = storage.refreshToken() ?: storage.accessToken()
        storage.clear()
        if (token != null) {
            val authorization = "Bearer $token"
            appScope.launch {
                if (!postLogout(authorization)) {
                    delay(LOGOUT_RETRY_DELAY_MS)
                    postLogout(authorization)
                }
            }
        }
    }

    /**
     * 서버 로그아웃 1회 시도. 네트워크 오류·비-2xx 모두 `false`.
     *
     * 도메인 에러로 래핑할 필요가 없는 best-effort 호출이라 [Outcome] 대신 plain try/catch.
     * 코루틴 취소는 비즈니스 실패가 아니므로 재던진다.
     */
    private suspend fun postLogout(authorization: String): Boolean = try {
        api.logout(authorization).isSuccessful
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        false
    }

    override suspend fun ensureSessionLoaded() = storage.warmUp()

    private companion object {
        const val LOGOUT_RETRY_DELAY_MS = 1_000L
    }
}

