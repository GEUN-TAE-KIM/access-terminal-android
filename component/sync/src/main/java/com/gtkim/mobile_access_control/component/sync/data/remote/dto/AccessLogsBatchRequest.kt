package com.gtkim.mobile_access_control.component.sync.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/access/logs/batch` 요청 (api-spec §5.2). 1~100건.
 */
@Serializable
internal data class AccessLogsBatchRequest(
    val items: List<AccessLogItem>,
)

@Serializable
internal data class AccessLogItem(
    val clientLogId: String,
    val cardUid: String,
    val cardType: String,
    val terminalId: String,
    val zone: String,
    val decidedAt: String,
    val result: String,
    val denyReason: String? = null,
    val verifierVersion: String,
)
