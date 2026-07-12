package com.gtkim.mobile_access_control.component.master.domain.model

import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.core.common.result.AppError

/**
 * master snapshot 동기화 도메인 에러 — `GET /api/v1/master/snapshot` 진입점에서 발생.
 *
 * master sync 는 background WorkManager 작업이라 UI 다이얼로그로 노출되지 않는다 — [message] 는
 * Timber 로그용 fallback 영문 문자열로 둔다. 다른 도메인 에러처럼 strings.xml 리소스 ID 를 잡지
 * 않는 이유. 향후 사용자 노출 surface 가 생기면 `UiText.Res` 로 이전한다.
 */
sealed interface MasterError : AppError {

    data object NoConnection : MasterError {
        override val message: UiText = UiText.Raw("master snapshot: no connection")
    }

    data object Timeout : MasterError {
        override val message: UiText = UiText.Raw("master snapshot: timeout")
    }

    /** 서버가 200/304 외 응답 + RFC 7807 errorCode (있으면 보존). */
    data class ServerError(val code: Int, val errorCode: String?) : MasterError {
        override val message: UiText =
            UiText.Raw("master snapshot: server $code ${errorCode ?: ""}")
    }

    /** 200 OK 응답인데 body 가 null — 서버 명세 위반 (§8.1). */
    data object EmptyBody : MasterError {
        override val message: UiText = UiText.Raw("master snapshot: 200 OK with empty body")
    }

    /** 200/304 외 status code — server 명세에 없는 응답. */
    data class UnexpectedStatus(val code: Int) : MasterError {
        override val message: UiText = UiText.Raw("master snapshot: unexpected status $code")
    }

    /** 분류 실패 — [cause] 는 Timber 로그용 진단 정보, 도메인 흐름 분기엔 사용 안 함. */
    data class Unknown(val cause: Throwable) : MasterError {
        override val message: UiText = UiText.Raw("master snapshot: unknown error")
    }
}
