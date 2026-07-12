package com.gtkim.mobile_access_control.feature.register.mapper

import com.gtkim.mobile_access_control.component.access.domain.model.CardError
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.register.R
import com.gtkim.mobile_access_control.feature.common.R as CommonR

/**
 * 카드 등록 화면용 도메인 에러 → UiError 매퍼 (presentation 레이어 책임).
 *
 * NFC 읽기 에러([NfcError]) 와 서버 등록 에러([CardError]) 두 진입점을 한 곳에서 응집해 매핑한다.
 * 서버 에러 메시지는 [CardError] 가 errorCode 별로 이미 사용자 노출 문구를 갖고 있어 [UiText.Raw]
 * 로 그대로 전달.
 */
internal fun AppError.toUiError(): UiError = when (this) {
    is NfcError -> toUiError()
    is CardError -> toUiError()
    else -> UiError(
        title = UiText.Res(CommonR.string.error_title_generic),
        message = message,
    )
}

internal fun CardError.toUiError(): UiError = when (this) {
    is CardError.SessionExpired -> UiError(
        title = UiText.Res(CommonR.string.error_title_session_expired),
        message = message,
    )
    // 나머지는 CardError.message 가 이미 적절한 한국어 문구를 보유 — 동일 타이틀로 묶는다.
    // sealed 분기는 `else` 없이 — 새 케이스 추가 시 컴파일러가 강제 분기 (architecture.md §4).
    is CardError.UserNotFound,
    is CardError.CardAlreadyRegistered,
    is CardError.UserInactive,
    is CardError.ForbiddenRole,
    is CardError.ValidationFailed,
    is CardError.RateLimited,
    is CardError.NoConnection,
    is CardError.Timeout,
    is CardError.ServerError,
    is CardError.Unknown,
    -> UiError(
        title = UiText.Res(R.string.register_error_card_failure_title),
        message = message,
    )
}

internal fun NfcError.toUiError(): UiError = when (this) {
    is NfcError.NotSupported -> UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_not_supported),
        message = UiText.Res(R.string.register_error_nfc_not_supported_message),
    )
    is NfcError.Disabled -> UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_disabled),
        message = UiText.Res(CommonR.string.error_nfc_message_disabled),
        confirmText = UiText.Res(CommonR.string.error_button_open_settings),
        action = UiError.Action.OpenNfcSettings,
    )
    is NfcError.Timeout -> UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_timeout),
        message = UiText.Res(CommonR.string.error_nfc_message_timeout),
    )
    is NfcError.UnreadableTag -> UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_unreadable),
        message = UiText.Res(R.string.register_error_nfc_unreadable_message),
    )
    is NfcError.TagLost -> UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_tag_lost),
        message = UiText.Res(CommonR.string.error_nfc_message_tag_lost),
    )
    is NfcError.Io -> UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_io),
        message = UiText.Res(CommonR.string.error_nfc_message_io),
    )
}
