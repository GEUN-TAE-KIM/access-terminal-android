package com.gtkim.mobile_access_control.component.history.data.mapper

import com.gtkim.mobile_access_control.component.history.data.remote.dto.LogAdminDto
import com.gtkim.mobile_access_control.component.history.data.remote.dto.LogItemDto
import com.gtkim.mobile_access_control.component.history.data.remote.dto.LogPageResponse
import com.gtkim.mobile_access_control.component.history.data.remote.dto.LogUserDto
import com.gtkim.mobile_access_control.component.history.domain.model.AccessLog
import com.gtkim.mobile_access_control.component.history.domain.model.LogAdmin
import com.gtkim.mobile_access_control.component.history.domain.model.LogCursor
import com.gtkim.mobile_access_control.component.history.domain.model.LogPage
import com.gtkim.mobile_access_control.component.history.domain.model.LogResult
import com.gtkim.mobile_access_control.component.history.domain.model.LogUser
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.DenyReason
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.core.model.TerminalId
import com.gtkim.mobile_access_control.core.model.Zone
import java.time.Instant

internal fun LogPageResponse.toDomain(): LogPage = LogPage(
    items = items.map { it.toDomain() },
    nextCursor = nextCursor?.let(::LogCursor),
    hasMore = hasMore,
)

private fun LogItemDto.toDomain(): AccessLog = AccessLog(
    id = id,
    cardUid = CardUid(cardUid),
    cardType = cardType,
    result = LogResult.fromWire(result),
    denyReason = denyReason?.let(DenyReason::fromWire),
    user = user?.toDomain(),
    admin = admin.toDomain(),
    terminalId = TerminalId(terminalId),
    zone = Zone(zone),
    attemptedAt = Instant.parse(attemptedAt),
)

private fun LogUserDto.toDomain(): LogUser = LogUser(
    id = id,
    employeeCode = EmployeeCode(employeeCode),
    name = name,
    department = department,
)

private fun LogAdminDto.toDomain(): LogAdmin = LogAdmin(
    id = id,
    username = username,
    name = name,
)
