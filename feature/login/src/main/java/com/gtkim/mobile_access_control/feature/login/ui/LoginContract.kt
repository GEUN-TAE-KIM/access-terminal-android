package com.gtkim.mobile_access_control.feature.login.ui

import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState

/**
 * 로그인 화면의 MVI 계약(Contract).
 * UI / ViewModel 양쪽이 합의해야 할 3-tuple 을 한 파일에 모은다.
 */
internal data class LoginUiState(
    val loginId: String = "",
    val password: String = "",
    val loading: Boolean = false,
    /**
     * 단말 네트워크 상태. offline 시 로그인 시도 자체를 차단 — 새 세션 시작이 의미 없는 상태에서
     * 로컬 socket inconsistency 로 의도치 않게 인증되는 경로를 봉인 (AppShell 의 sync 동일 패턴).
     */
    val isOnline: Boolean = true,
    val dialog: DialogState? = null,
) {
    /**
     * 로그인 버튼 / IME Done 발사 활성화 조건. 클라 측 가드는 빈 입력 + 네트워크 상태만 — 길이·형식
     * 검증은 서버가 `REQUEST_VALIDATION_FAILED` 로 반환하면 다이얼로그로 노출한다 (API 명세 §3.1).
     */
    val canSubmit: Boolean
        get() = !loading && isOnline && loginId.isNotBlank() && password.isNotBlank()
}

internal sealed interface LoginIntent {
    data class IdChanged(
        val value: String,
    ) : LoginIntent
    data class PasswordChanged(
        val value: String,
    ) : LoginIntent
    data object Submit : LoginIntent
    data object DialogDismissed : LoginIntent
    data object DialogConfirmed : LoginIntent
}

internal sealed interface LoginSideEffect {
    data object NavigateToScan : LoginSideEffect
}
