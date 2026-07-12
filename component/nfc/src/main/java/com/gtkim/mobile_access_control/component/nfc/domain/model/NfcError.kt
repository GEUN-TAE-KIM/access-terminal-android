package com.gtkim.mobile_access_control.component.nfc.domain.model

import com.gtkim.mobile_access_control.component.nfc.R
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError

sealed interface NfcError : AppError {
    data object NotSupported : NfcError {
        override val message: UiText = UiText.Res(R.string.nfc_error_not_supported)
    }

    data object Disabled : NfcError {
        override val message: UiText = UiText.Res(R.string.nfc_error_disabled)
    }

    data object Timeout : NfcError {
        override val message: UiText = UiText.Res(R.string.nfc_error_timeout)
    }

    data object UnreadableTag : NfcError {
        override val message: UiText = UiText.Res(R.string.nfc_error_unreadable_tag)
    }

    /**
     * 읽기 도중 카드가 리더에서 떨어진 경우. android.nfc.TagLostException 매핑.
     * 사용자에게 "다시 댔다 떼지 말고 잠시 대기해 주세요" 안내가 적절.
     */
    data object TagLost : NfcError {
        override val message: UiText = UiText.Res(R.string.nfc_error_tag_lost)
    }

    /**
     * Tag.connect / transceive 도중 IOException 등. [cause] 는 진단용으로 보존하지만 사용자
     * 노출 [message] 는 일반화한다 — raw 예외 텍스트(영문, stack trace 잔재)가 다이얼로그에
     * 그대로 노출되는 것을 막는다.
     */
    data class Io(val cause: Throwable) : NfcError {
        override val message: UiText = UiText.Res(R.string.nfc_error_io)
    }
}
