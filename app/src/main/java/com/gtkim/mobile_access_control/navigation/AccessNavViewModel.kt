package com.gtkim.mobile_access_control.navigation

import androidx.lifecycle.ViewModel
import com.gtkim.mobile_access_control.component.auth.domain.model.AuthState
import com.gtkim.mobile_access_control.component.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Outer NavGraph 의 인증 라이프사이클 책임 ViewModel.
 *
 * 토큰 영속 상태([AuthRepository.authState])를 그대로 노출한다. NavGraph 의 LaunchedEffect 가 본
 * StateFlow 를 관찰해 [AuthState.LoggedOut] 진입 시 LoginRoute 로 popUpTo 한다 — UI 쪽 콜백
 * 왕복(SideEffect → callback → signOut → callback → navigate)을 단방향 stream 으로 통합.
 *
 * 토큰 클리어 자체는 본 ViewModel 의 책임이 아니다. 두 가지 경로가 모두 [AuthState] 를 LoggedOut
 * 으로 전이시킨다:
 *  - AuthInterceptor 의 refresh 실패(`AuthTokenProvider.refreshTokens`)가 직접 storage.clear() 호출
 *  - 각 feature ViewModel 이 UiError.Action.Reauthenticate 처리 시 LogoutUseCase 호출
 */
@HiltViewModel
internal class AccessNavViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.authState
}
