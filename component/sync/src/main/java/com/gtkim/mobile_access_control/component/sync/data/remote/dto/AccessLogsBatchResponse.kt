package com.gtkim.mobile_access_control.component.sync.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/access/logs/batch` 응답 (api-spec §5.2).
 *
 * 부분 성공도 200 으로 응답된다 — [rejected] 에 거부 항목 (24h 초과 등) 만 보고하고, accepted /
 * duplicates 둘 다 큐에서 제거한다 (서버에 row 존재). [serverEvaluation] 은 drift 감사 보고
 * (server PermissionEvaluator 가 같은 입력 재평가한 결과).
 *
 * wire 의 `accepted` / `duplicates` 카운트는 ignoreUnknownKeys 로 무시 — 큐 제거 로직은
 * "rejected 가 아닌 모두 제거" 로 충분.
 */
@Serializable
internal data class AccessLogsBatchResponse(
    val rejected: List<RejectedItem> = emptyList(),
    val serverEvaluation: List<ServerEvaluation> = emptyList(),
)

@Serializable
internal data class RejectedItem(
    val clientLogId: String,
    val errorCode: String,
    val reason: String? = null,
)

@Serializable
internal data class ServerEvaluation(
    val clientLogId: String,
    val serverLogId: Long,
    val agreement: Boolean,
)
