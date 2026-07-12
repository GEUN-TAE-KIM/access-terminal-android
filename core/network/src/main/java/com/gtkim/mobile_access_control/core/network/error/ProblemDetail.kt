package com.gtkim.mobile_access_control.core.network.error

import kotlinx.serialization.Serializable

/**
 * RFC 7807 Problem Details for HTTP APIs + 본 API 의 커스텀 확장.
 *
 * 에러 응답(`application/problem+json`)의 본문 포맷. 클라이언트는 [errorCode] 로 분기하며,
 * 표준 필드(`type`/`title`/`status`/`detail`/`instance`)는 진단·로깅 용도다.
 *
 * 서버가 보장하더라도 방어적으로 전부 nullable 로 둔다 — 손상된 에러 바디에도 파싱이 깨지지 않도록.
 */
@Serializable
data class ProblemDetail(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
    /** 커스텀 확장 — 클라이언트 분기의 단일 기준. 예: `AUTH_INVALID_CREDENTIALS`. */
    val errorCode: String? = null,
    /** 커스텀 확장 — 동일 요청 재시도가 의미 있는지. */
    val retryable: Boolean = false,
    /** 커스텀 확장 — 요청 trace ID echo. */
    val traceId: String? = null,
    /** 커스텀 확장 — 에러 발생 시각 (ISO 8601 UTC). */
    val timestamp: String? = null,
)
