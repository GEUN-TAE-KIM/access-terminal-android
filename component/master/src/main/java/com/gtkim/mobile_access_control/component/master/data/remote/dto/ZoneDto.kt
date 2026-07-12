package com.gtkim.mobile_access_control.component.master.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * snapshot 응답의 zone upserted 항목 (api-spec §8.1, Phase 12 server-side).
 *
 * [code] = 단말이 verify 요청 body 의 `zone` 필드로 보낼 식별자 (예: `GATE-A`).
 * [name] = picker UI 에 표시할 사람-친화 라벨 (예: `正面ゲート`).
 */
@Serializable
internal data class ZoneDto(
    val id: Long,
    val code: String,
    val name: String,
)
