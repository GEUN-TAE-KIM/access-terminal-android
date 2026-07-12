package com.gtkim.mobile_access_control.component.access.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class VerifyRequest(
    val cardUid: String,
    val cardType: String,
    val terminalId: String,
    val zone: String,
    /** ISO 8601 UTC (`Instant.toString()`). 서버 시각 ±5분 이내. */
    val timestamp: String,
    /** 16바이트 hex (32자) 랜덤. 매 요청 새로 생성 — 재시도 포함 (API 명세 §2 Replay 방어). */
    val nonce: String,
)
