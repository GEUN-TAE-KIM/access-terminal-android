package com.gtkim.mobile_access_control.core.database.sync.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 오프라인 검문 결과 1건. 네트워크 복구 시 `POST /api/v1/access/logs/batch` (api-spec §5.2) 로
 * 일괄 업로드된다.
 *
 * - [id] = `clientLogId` (UUID v4). 서버 멱등 키. 동일 키 재전송은 서버가 dedup.
 * - [cardType] = api-spec §5.2 의 필수 필드. audit 일관성을 위해 검문 시점의 카드 타입 보존.
 * - [verifierVersion] = 판정에 사용한 master snapshot ETag. drift 감사용 (§5.2 `serverEvaluation`).
 * - [result] / [denyReason] = `AccessResult` / `DenyReason` enum 이름 (wire 와 동일).
 * - [terminalId] = 단말 식별자. 업로드 시 본문에 포함 — 큐 시점의 값을 보존해 후에 단말 ID 가 바뀌어도
 *   audit 원본을 유지한다.
 */
@Entity(tableName = "pending_logs")
data class PendingLogEntity(
    @PrimaryKey val id: String,
    val cardUid: String,
    val cardType: String,
    val terminalId: String,
    val zone: String,
    val decidedAtEpochMs: Long,
    val result: String,
    val denyReason: String?,
    val verifierVersion: String,
    val attempts: Int,
)
