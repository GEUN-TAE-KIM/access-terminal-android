package com.gtkim.mobile_access_control.component.master.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * snapshot 응답의 card upserted 항목 (api-spec §8.1).
 *
 * wire 의 [cardType] 은 `FELICA`/`ISO_DEP`/`NDEF` 만 (§8.1 MOCK cardType 제외). 판정은 [isActive] 우선.
 */
@Serializable
internal data class CardDto(
    val id: Long,
    val cardUid: String,
    val cardType: String,
    val userId: Long,
    val isActive: Boolean,
)
