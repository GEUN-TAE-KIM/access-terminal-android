package com.gtkim.mobile_access_control.component.master.data.error

import com.gtkim.mobile_access_control.component.master.domain.model.MasterError
import com.gtkim.mobile_access_control.core.network.error.NetworkError
import com.gtkim.mobile_access_control.core.network.error.toNetworkError

/**
 * Data 레이어 에러 매핑 — `Throwable` → [MasterError].
 *
 * 다른 component 의 *ErrorMapper 와 동일 패턴 — 전송 계층 오류는 [toNetworkError] 로 분류하고,
 * 서버 에러는 RFC 7807 `errorCode` 가 있으면 그대로 보존 (master snapshot 의 5xx 등 미세 분기는
 * 본 시점 명세 외 — server 추가되면 여기서 분기 추가).
 */
internal fun Throwable.toMasterError(): MasterError = when (val net = toNetworkError()) {
    is NetworkError.NoConnection -> MasterError.NoConnection
    is NetworkError.Timeout -> MasterError.Timeout
    is NetworkError.Unknown -> MasterError.Unknown(net.cause)
    is NetworkError.Server -> MasterError.ServerError(net.code, net.detail?.errorCode)
}
