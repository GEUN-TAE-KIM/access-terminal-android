package com.gtkim.mobile_access_control.core.network.error

/**
 * 전송 계층(Retrofit/OkHttp) Throwable 의 **내부 분류**용 sealed 타입.
 *
 * 도메인 에러가 아니라 `core.common.result.AppError` 를 구현하지 않는다 — 각 `:component:*` 의
 * data 레이어 매퍼 (`*ErrorMapper.kt`) 가 본 분류를 받아 즉시 자기 도메인 에러 (AuthError /
 * AccessError / CardError / HistoryError / StatsError) 로 변환한 뒤 호출자에게 `Outcome.Failure`
 * 로 노출한다. 따라서 본 타입은 data 레이어 경계 밖으로 새지 않으며, 사용자 노출 메시지
 * (`core.common.error.UiText`) 도 갖지 않는다.
 *
 * 사용자 메시지는 매핑된 도메인 에러가 자체적으로 `UiText.Res(...)` 로 보유한다
 * (예: `component.auth.domain.model.AuthError.NoConnection`).
 */
sealed interface NetworkError {
    data object NoConnection : NetworkError
    data object Timeout : NetworkError
    data class Server(val code: Int, val detail: ProblemDetail?) : NetworkError
    data class Unknown(val cause: Throwable) : NetworkError
}
