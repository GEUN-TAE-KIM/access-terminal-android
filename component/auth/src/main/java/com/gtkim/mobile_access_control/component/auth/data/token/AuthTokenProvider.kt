package com.gtkim.mobile_access_control.component.auth.data.token

import com.gtkim.mobile_access_control.component.auth.data.local.TokenStorage
import com.gtkim.mobile_access_control.component.auth.data.remote.AuthApi
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.network.auth.TokenProvider
import com.gtkim.mobile_access_control.core.network.error.NetworkError
import com.gtkim.mobile_access_control.core.network.error.toNetworkError
import kotlinx.coroutines.CancellationException
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * [TokenProvider] 구현 — `AuthInterceptor` 의 토큰 seam.
 *
 * Refresh Token Rotation(§2): refresh 성공 시 새 Access + 새 Refresh 를 함께 저장한다.
 */
internal class AuthTokenProvider @Inject constructor(
    private val api: AuthApi,
    private val storage: TokenStorage,
    private val time: TimeProvider,
) : TokenProvider {

    override fun currentAccessToken(): String? = storage.accessToken()

    override fun accessTokenExpiresAt(): Instant? = storage.accessTokenExpiresAt()

    override suspend fun refreshTokens(): Boolean {
        val refreshToken = storage.refreshToken() ?: return false
        return try {
            val res = api.refresh(authorization = "Bearer $refreshToken")
            val now = time.now()
            storage.save(
                accessToken = res.accessToken,
                refreshToken = res.refreshToken,
                accessExpiresAt = now.plus(Duration.ofSeconds(res.accessTokenExpiresIn)),
                refreshExpiresAt = now.plus(Duration.ofSeconds(res.refreshTokenExpiresIn)),
            )
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // 서버가 Refresh Token 을 거절(만료·폐기 — 401)했으면 세션 종료 → 로컬 토큰 파기.
            // 네트워크 오류면 토큰을 유지하고 다음 기회에 재시도한다.
            if (e.isRefreshRejected()) storage.clear()
            false
        }
    }

    private fun Throwable.isRefreshRejected(): Boolean {
        val net = toNetworkError()
        return net is NetworkError.Server && net.code == HTTP_UNAUTHORIZED
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
