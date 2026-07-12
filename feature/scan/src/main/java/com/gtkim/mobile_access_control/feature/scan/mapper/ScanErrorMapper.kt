package com.gtkim.mobile_access_control.feature.scan.mapper

import com.gtkim.mobile_access_control.component.access.domain.model.AccessError
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.scan.R
import com.gtkim.mobile_access_control.feature.common.R as CommonR

/**
 * Scan 화면용 도메인 에러 → [UiError] 매퍼 (presentation 레이어 책임).
 *
 * VM 본체에서 매핑 로직을 분리해 본 파일에 응집한다 — 새 도메인 에러 종류가 추가될 때 변경 지점이
 * 한 곳으로 모인다. catch-all `else` 금지 — sealed exhaustive 강제 (architecture.md §16).
 *
 * [NfcError.Disabled] 만 사용자가 직접 해결 가능한 액션(NFC 설정 이동)을 부여하며, 검증 흐름의
 * [AccessError.SessionExpired] 는 별도 액션(재인증)으로 분기한다.
 *
 * 텍스트는 [UiText] 추상화 — 정적 라벨은 [UiText.Res] 로 리소스 ID 참조, 서버 응답 메시지처럼
 * 런타임 결정 문자열은 [UiText.Raw]. 최종 해석은 AppDialog 가 수행.
 */
internal fun AppError.toUiError(): UiError = when (this) {
    is NfcError -> toUiError()
    is AccessError -> toUiError()
    else -> UiError(
        title = UiText.Res(CommonR.string.error_title_generic),
        message = UiText.Res(CommonR.string.error_message_unknown),
    )
}

internal fun NfcError.toUiError(): UiError = when (this) {
    is NfcError.NotSupported -> UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_not_supported),
        message = UiText.Res(R.string.scan_error_nfc_not_supported_message),
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
        message = UiText.Res(R.string.scan_error_nfc_unreadable_message),
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

private fun AccessError.toUiError(): UiError = when (this) {
    is AccessError.CardNotRegistered -> UiError(
        // 검문 단말 핵심 UX — 미등록 카드는 거부 결과가 아니라 등록 절차 안내 톤으로 분리.
        title = UiText.Res(R.string.scan_error_card_not_registered_title),
        message = UiText.Res(R.string.scan_error_card_not_registered_message),
    )

    is AccessError.ReplayDetected -> UiError(
        title = UiText.Res(R.string.scan_error_replay_title),
        message = UiText.Res(R.string.scan_error_replay_message),
    )

    is AccessError.TimestampSkew -> UiError(
        title = UiText.Res(R.string.scan_error_clock_skew_title),
        message = UiText.Res(R.string.scan_error_clock_skew_message),
    )

    is AccessError.IdempotencyConflict -> UiError(
        title = UiText.Res(R.string.scan_error_idempotency_title),
        message = UiText.Res(R.string.scan_error_idempotency_message),
    )

    is AccessError.InvalidCardType -> UiError(
        title = UiText.Res(R.string.scan_error_invalid_card_type_title),
        message = UiText.Res(R.string.scan_error_invalid_card_type_message),
    )
    // 정상 흐름이면 클라가 사전 검증·헤더 부착을 보장하므로 도달 시 클라 버그 신호.
    // 사용자에게는 일반 오류 톤으로만 노출 — 진단은 로그/traceId 로.
    is AccessError.ValidationFailed,
    is AccessError.MissingHeader -> UiError(
        title = UiText.Res(R.string.scan_error_validation_title),
        message = message,
    )

    is AccessError.SessionExpired -> UiError(
        title = UiText.Res(CommonR.string.error_title_session_expired),
        message = message,
        confirmText = UiText.Res(CommonR.string.error_button_reauthenticate),
        action = UiError.Action.Reauthenticate,
    )

    is AccessError.RateLimited -> UiError(
        title = UiText.Res(CommonR.string.error_title_rate_limited),
        message = message,
    )

    is AccessError.NoConnection -> UiError(
        title = UiText.Res(CommonR.string.error_title_no_connection),
        message = message,
    )

    is AccessError.Timeout -> UiError(
        title = UiText.Res(CommonR.string.error_title_timeout),
        message = message,
    )

    is AccessError.ServerError -> UiError(
        title = UiText.Res(CommonR.string.error_title_server),
        message = message,
    )

    is AccessError.Unknown -> UiError(
        title = UiText.Res(R.string.scan_error_verify_failed_title),
        message = message,
    )
}
