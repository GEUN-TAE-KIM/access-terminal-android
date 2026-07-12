package com.gtkim.mobile_access_control.feature.common.ui.label

import androidx.annotation.StringRes
import com.gtkim.mobile_access_control.core.model.DenyReason
import com.gtkim.mobile_access_control.feature.common.R

/**
 * 거부 사유 enum → 사용자 노출 한국어 라벨 리소스 ID. scan/history/stats 3개 화면이 공유.
 *
 * wire 포맷 (`ACCESS_DENIED_*` 문자열) ↔ enum 변환은 data 레이어 [DenyReason.fromWire] 가 책임지므로
 * 본 함수는 enum 만 다룬다. 라벨 문자열은 본 모듈의 strings.xml 에 정의된다.
 *
 * `when` 은 exhaustive — `else` 금지 (architecture.md §4). 새 enum 케이스 추가 시 컴파일러가 매핑 누락을
 * 즉시 catch.
 */
@StringRes
fun DenyReason.toKoreanLabelRes(): Int = when (this) {
    DenyReason.NO_PERMISSION_FOR_ZONE -> R.string.deny_reason_no_permission_for_zone
    DenyReason.OUT_OF_ALLOWED_HOURS -> R.string.deny_reason_out_of_allowed_hours
    DenyReason.PERMISSION_EXPIRED -> R.string.deny_reason_permission_expired
    DenyReason.CARD_REVOKED -> R.string.deny_reason_card_revoked
    DenyReason.USER_INACTIVE -> R.string.deny_reason_user_inactive
    DenyReason.CARD_NOT_REGISTERED -> R.string.deny_reason_card_not_registered
    DenyReason.UNKNOWN -> R.string.deny_reason_unknown
}
