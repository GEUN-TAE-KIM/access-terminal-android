package com.gtkim.mobile_access_control.component.master.domain.model

import com.gtkim.mobile_access_control.core.model.CardUid

/**
 * 캐시된 card. server snapshot 의 raw 필드 (api-spec §8.1).
 *
 * 판정은 [isActive] 우선. [userId] 는 permission join 키.
 */
data class CachedCard(
    val id: Long,
    val uid: CardUid,
    val cardType: String,
    val userId: Long,
    val isActive: Boolean,
)
