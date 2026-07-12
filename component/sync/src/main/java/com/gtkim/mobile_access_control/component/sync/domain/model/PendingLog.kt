package com.gtkim.mobile_access_control.component.sync.domain.model

import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import java.time.Instant
import java.util.UUID

/**
 * 오프라인에서 LocalAccessVerifier 가 자체 판정한 검문 결과 1건. 네트워크 복구 시
 * `POST /api/v1/access/logs/batch` (api-spec §5.2) 로 일괄 업로드된다.
 *
 * 필드는 §5.2 요청 본문 1:1 매핑:
 *  - [id] = `clientLogId` (UUID v4) — 서버 멱등 키.
 *  - [cardType] / [terminalId] / [zone] / [decidedAt] / [result] / [denyReason] = 그대로.
 *  - [verifierVersion] = 판정에 사용한 master snapshot ETag (drift 감사용, §5.2).
 *
 * [result] / [denyReason] 은 `AccessDecision` / `DenyReason` enum 이름 (wire 값과 동일).
 */
data class PendingLog(
    val id: UUID,
    val cardUid: CardUid,
    val cardType: String,
    val terminalId: String,
    val zone: Zone,
    val decidedAt: Instant,
    val result: String,
    val denyReason: String?,
    val verifierVersion: String,
)
