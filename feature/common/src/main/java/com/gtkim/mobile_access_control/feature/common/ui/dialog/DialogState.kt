package com.gtkim.mobile_access_control.feature.common.ui.dialog

import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError

/**
 * 화면 UiState 에 포함되는 다이얼로그 상태.
 *
 * 글로벌 컨트롤러를 두지 않고 각 화면이 자기 UiState 안에서 `dialog: DialogState?` 로 보유한다 —
 * MVI 의 단방향 + 단일 진실 source 원칙을 화면 단위로 유지하기 위한 설계.
 *
 * 공용 디자인은 [AppDialog] 컴포저블에서 흡수하므로 모든 화면이 동일 외관으로 렌더된다.
 */
sealed interface DialogState {
    /**
     * 도메인 에러 노출 다이얼로그. 확인 버튼이 [UiError.Action] 에 따라 추가 액션을 트리거할 수 있다
     * (예: NFC 설정 열기, 재로그인).
     */
    data class Error(
        val uiError: UiError,
    ) : DialogState

    /**
     * 운영자에게 작업의 진행을 명시적으로 확인받는 다이얼로그. 확인 버튼은 호출 화면이 정의한
     * 인텐트를 발사한다 (예: "미전송 큐 즉시 전송하시겠습니까?" → 확인 시 flush 인텐트).
     *
     * @param confirmLabel null 이면 공통 "확인" 라벨로 폴백.
     */
    data class Confirm(
        val title: UiText,
        val message: UiText,
        val confirmLabel: UiText? = null,
    ) : DialogState

    /**
     * 사용자 액션이 필요 없는 단순 안내 다이얼로그. 확인 = 다이얼로그 닫기.
     * (예: 오프라인 상태에서 전송이 불가능함을 안내)
     */
    data class Info(
        val title: UiText,
        val message: UiText,
    ) : DialogState
}
