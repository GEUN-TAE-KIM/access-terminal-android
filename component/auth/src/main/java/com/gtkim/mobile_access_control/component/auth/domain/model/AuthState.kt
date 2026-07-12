package com.gtkim.mobile_access_control.component.auth.domain.model

/**
 * 인증 세션 상태. 영속 토큰 보유 여부 1:1.
 *
 * UI/Navigation 이 관찰해 단방향으로 반응한다 — refresh 실패로 [AuthTokenProvider] 가
 * 토큰을 폐기하거나, 명시적 logout 으로 토큰이 비워지면 [LoggedOut] 로 전이된다.
 */
sealed interface AuthState {
    data object LoggedIn : AuthState
    data object LoggedOut : AuthState
}
