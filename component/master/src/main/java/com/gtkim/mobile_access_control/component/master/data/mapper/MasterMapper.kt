package com.gtkim.mobile_access_control.component.master.data.mapper

import com.gtkim.mobile_access_control.component.master.data.remote.dto.CardDto
import com.gtkim.mobile_access_control.component.master.data.remote.dto.PermissionDto
import com.gtkim.mobile_access_control.component.master.data.remote.dto.UserDto
import com.gtkim.mobile_access_control.component.master.data.remote.dto.ZoneDto
import com.gtkim.mobile_access_control.component.master.domain.model.CachedCard
import com.gtkim.mobile_access_control.component.master.domain.model.CachedPermission
import com.gtkim.mobile_access_control.component.master.domain.model.CachedUser
import com.gtkim.mobile_access_control.component.master.domain.model.CachedZone
import com.gtkim.mobile_access_control.core.database.master.entity.CardEntity
import com.gtkim.mobile_access_control.core.database.master.entity.PermissionEntity
import com.gtkim.mobile_access_control.core.database.master.entity.UserEntity
import com.gtkim.mobile_access_control.core.database.master.entity.ZoneEntity
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.core.model.Zone
import java.time.Instant
import java.time.LocalTime

// DTO → Entity — wire 의 PII (name / department) 는 의도적으로 drop (hybrid-offline.md §2.1).

internal fun UserDto.toEntity() = UserEntity(
    id = id,
    employeeCode = employeeCode,
    isActive = isActive,
)

internal fun CardDto.toEntity() = CardEntity(
    id = id,
    cardUid = cardUid,
    cardType = cardType,
    userId = userId,
    isActive = isActive,
)

internal fun PermissionDto.toEntity() = PermissionEntity(
    id = id,
    userId = userId,
    zone = zone,
    validUntilEpochMs = validUntil?.let { Instant.parse(it).toEpochMilli() },
    allowedHoursStart = allowedHoursStart,
    allowedHoursEnd = allowedHoursEnd,
)

internal fun ZoneDto.toEntity() = ZoneEntity(
    id = id,
    code = code,
    name = name,
)

// Entity → Domain

internal fun UserEntity.toDomain() = CachedUser(
    id = id,
    employeeCode = EmployeeCode(employeeCode),
    isActive = isActive,
)

internal fun CardEntity.toDomain() = CachedCard(
    id = id,
    uid = CardUid(cardUid),
    cardType = cardType,
    userId = userId,
    isActive = isActive,
)

internal fun PermissionEntity.toDomain() = CachedPermission(
    id = id,
    userId = userId,
    zone = Zone(zone),
    validUntil = validUntilEpochMs?.let(Instant::ofEpochMilli),
    allowedHoursStart = allowedHoursStart?.let(LocalTime::parse),
    allowedHoursEnd = allowedHoursEnd?.let(LocalTime::parse),
)

internal fun ZoneEntity.toDomain() = CachedZone(
    id = id,
    code = Zone(code),
    name = name,
)
