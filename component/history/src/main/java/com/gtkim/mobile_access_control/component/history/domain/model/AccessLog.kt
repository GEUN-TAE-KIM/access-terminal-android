package com.gtkim.mobile_access_control.component.history.domain.model

import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.DenyReason
import com.gtkim.mobile_access_control.core.model.TerminalId
import com.gtkim.mobile_access_control.core.model.Zone
import java.time.Instant

/**
 * 출입 기록 한 건 (API 명세 §5.1 응답 `items[]`).
 */
data class AccessLog(
    /** 출입 기록 PK (`access_logs.id`). */
    val id: Long,
    val cardUid: CardUid,
    /** 카드 타입 표시용 문자열 — `FELICA` / `ISO_DEP` / `MOCK` / `NDEF`. */
    val cardType: String,
    val result: LogResult,
    /** 거부 사유. [result] 가 허용이면 `null`. */
    val denyReason: DenyReason?,
    /** 카드 등록 사용자. 미등록 카드 기록이면 `null`. */
    val user: LogUser?,
    /** 검문을 수행한 관리자. */
    val admin: LogAdmin,
    val terminalId: TerminalId,
    val zone: Zone,
    /** 검증 시도가 발생한 시각 (`access_logs.attempted_at`, UTC). */
    val attemptedAt: Instant,
)
