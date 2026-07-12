package com.gtkim.mobile_access_control.feature.common.ui

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost

/**
 * 모든 화면별 ViewModel 의 공통 베이스.
 * Orbit ContainerHost 기반으로 UiState / SideEffect / Intent 3-tuple 을 표준화한다.
 *
 * - UiState: Compose 가 렌더링할 단일 상태 모델 (immutable data class)
 * - SideEffect: 1회성 효과 (navigation, snackbar, sound 등) — state 에 담지 않을 것
 * - Intent: 사용자 입력·시스템 이벤트의 sealed 표현. 화면은 `onIntent(intent)` 한 곳으로만 dispatch.
 *
 * Screen 은 절대 ViewModel 의 구체 메서드를 직접 호출하지 않는다.
 * `viewModel.onIntent(MyIntent.Click)` 형태로 단일 진입점을 통해서만 상호작용한다.
 */
abstract class BaseViewModel<UiState : Any, SideEffect : Any, Intent : Any> :
    ViewModel(),
    ContainerHost<UiState, SideEffect> {

    abstract fun onIntent(intent: Intent)
}
