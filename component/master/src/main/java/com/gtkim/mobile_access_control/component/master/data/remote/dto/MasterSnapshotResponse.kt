package com.gtkim.mobile_access_control.component.master.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/master/snapshot` 응답 (api-spec §8.1).
 *
 * [version] 은 ETag 와 동일 값. 각 섹션은 [Section.upserted] (생성·변경분) 과
 * [Section.deletedIds] (tombstone) 로 나뉜다.
 *
 * 평가는 client 가 raw 필드로 수행한다 — `validUntil` / `allowedHoursStart`·`End` 가 그대로 내려온다.
 *
 * wire 의 `generatedAt` 은 ignoreUnknownKeys 로 무시 — 현재 UI 가 표시할 자리가 없다.
 */
@Serializable
internal data class MasterSnapshotResponse(
    val version: String,
    val users: UsersSection,
    val cards: CardsSection,
    val permissions: PermissionsSection,
    // Phase 12 (server-side) — zone catalog. 구버전 서버 호환을 위해 default empty.
    val zones: ZonesSection = ZonesSection(),
) {

    @Serializable
    internal data class UsersSection(
        val upserted: List<UserDto> = emptyList(),
        val deletedIds: List<Long> = emptyList(),
    )

    @Serializable
    internal data class CardsSection(
        val upserted: List<CardDto> = emptyList(),
        val deletedIds: List<Long> = emptyList(),
    )

    @Serializable
    internal data class PermissionsSection(
        val upserted: List<PermissionDto> = emptyList(),
        val deletedIds: List<Long> = emptyList(),
    )

    @Serializable
    internal data class ZonesSection(
        val upserted: List<ZoneDto> = emptyList(),
        val deletedIds: List<Long> = emptyList(),
    )
}
