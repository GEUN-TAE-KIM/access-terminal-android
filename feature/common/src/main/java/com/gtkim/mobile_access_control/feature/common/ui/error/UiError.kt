package com.gtkim.mobile_access_control.feature.common.ui.error

import com.gtkim.mobile_access_control.core.common.error.UiText

/**
 * 화면 공통 에러 다이얼로그 모델.
 *
 * 도메인 에러(AppError)는 각 feature 의 ViewModel 이 UiError 로 매핑한다 —
 * feature/common 은 도메인 에러 타입을 모름. UiError 자체는 어떤 도메인 모듈에도 의존하지 않는다.
 *
 * 텍스트는 [UiText] 추상화를 사용 — 정적 라벨은 [UiText.Res], 서버 응답 같은 동적 메시지는
 * [UiText.Raw]. 최종 String 해석은 Composable 진입점 (feature.common.ui.dialog.AppDialog) 에서.
 *
 * - [action] 이 None 이 아니면 다이얼로그의 확인 버튼이 추가 액션(예: NFC 설정 열기)을 트리거.
 *   ViewModel 은 ErrorDialogConfirmed Intent 를 받아 해당 SideEffect 를 발사.
 */
data class UiError(
    val title: UiText,
    val message: UiText,
    /** null 이면 AppDialog 가 공통 "확인" 라벨로 폴백. */
    val confirmText: UiText? = null,
    val action: Action = Action.None,
) {
    sealed interface Action {
        data object None : Action
        data object OpenNfcSettings : Action

        /**
         * 세션이 끊겨 재로그인이 필요. 확인 시 토큰 폐기 + 로그인 화면으로 이동.
         *
         * 인증 라우팅은 **반응형** — 각 화면 ViewModel 은 본 액션을 받아 `LogoutUseCase` 를 직접
         * 호출하기만 한다 (별도 SideEffect 채널 없음). 토큰이 비워지면 `AuthRepository.authState`
         * StateFlow 가 LoggedOut 으로 전이되고, `AccessNavGraph` 의 LaunchedEffect 가 그 변화를
         * 관찰해 LoginRoute 로 popUpTo 한다. AuthInterceptor 의 refresh 실패 경로
         * (`AuthTokenProvider`) 도 같은 StateFlow 를 통해 자동 복귀에 합류한다.
         */
        data object Reauthenticate : Action
    }
}
