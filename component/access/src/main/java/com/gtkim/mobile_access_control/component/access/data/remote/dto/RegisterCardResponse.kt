package com.gtkim.mobile_access_control.component.access.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/access/cards` 응답 (api-spec §4.2).
 *
 * wire 의 `cardId` / `cardType` / `isActive` / `issuedAt` 은 ignoreUnknownKeys 로 무시 — 본 UI 는
 * cardUid + user 만 등록 완료 표시에 사용.
 */
@Serializable
internal data class RegisterCardResponse(
    val cardUid: String,
    val user: CardUserDto,
)
