package com.gtkim.mobile_access_control.component.auth.domain.repository

import com.gtkim.mobile_access_control.component.auth.domain.model.Admin
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthError
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthState
import com.gtkim.mobile_access_control.core.common.result.Outcome
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    /**
     * 인증 세션 상태 스트림. 토큰 영속 캐시([com.gtkim.mobile_access_control.component.auth.data.local.TokenStorage])
     * 에 1:1 매핑되어 refresh 실패(인터셉터의 자동 토큰 폐기)·logout 호출 모두에 반응한다.
     * Navigation 레이어가 본 Flow 만 관찰하면 인증 변화에 대한 단방향 라우팅이 완성된다.
     */
    val authState: StateFlow<AuthState>

    suspend fun login(loginId: String, password: String): Outcome<Admin, AuthError>
    suspend fun logout()

    /**
     * 영속 세션(토큰)이 메모리 캐시에 반영될 때까지 대기.
     *
     * 콜드스타트 직후 첫 인증된 HTTP 요청이 빈 토큰으로 나가는 race 를 방지하기 위해
     * 부트스트랩 시점에 1회 호출한다. 이미 로드된 상태면 즉시 반환.
     */
    suspend fun ensureSessionLoaded()
}
