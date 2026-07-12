package com.gtkim.mobile_access_control.navigation

import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState

/**
 * AppShell 의 UiState — 단말 전역 상태(큐 카운트 / 네트워크 / 동기화 confirm 다이얼로그)를 보유.
 * 화면(Scan/Register/History/Stats) 어느 곳에 있든 TopBar 가 같은 정보를 노출한다.
 */
internal data class AppShellUiState(
    val pendingCount: Int = 0,
    val isOnline: Boolean = true,
    val dialog: DialogState? = null,
)

internal sealed interface AppShellSideEffect {
    /** 수동 flush 성공 — 스낵바로 운영자에게 알림. */
    data object ShowFlushSucceeded : AppShellSideEffect

    /** 수동 flush 실패 — 스낵바로 운영자에게 알림. */
    data object ShowFlushFailed : AppShellSideEffect

    /** 큐가 한도(100건) 초과로 가장 오래된 항목 폐기됨 — 스낵바 알림. */
    data object ShowQueueOverflow : AppShellSideEffect

    /** 개별 항목이 재시도 한도(10회) 초과로 dead-letter 폐기됨 — 스낵바 알림. */
    data object ShowQueueDeadLetter : AppShellSideEffect
}

internal sealed interface AppShellIntent {
    /** TopBar 동기화 아이콘 탭 — Confirm 다이얼로그 노출. */
    data object RequestSync : AppShellIntent

    /** Confirm 다이얼로그의 확인 버튼 — 큐 플러시 + master sync 묶음 enqueue. */
    data object ConfirmSync : AppShellIntent

    /** Confirm 다이얼로그의 취소 / dismiss. */
    data object DismissDialog : AppShellIntent

    /** Drawer 의 "로그아웃" 항목. */
    data object Logout : AppShellIntent
}
