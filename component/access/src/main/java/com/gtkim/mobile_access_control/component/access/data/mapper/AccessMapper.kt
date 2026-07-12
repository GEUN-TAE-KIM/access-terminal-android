package com.gtkim.mobile_access_control.component.access.data.mapper

import com.gtkim.mobile_access_control.component.access.data.remote.dto.CardUserDto
import com.gtkim.mobile_access_control.component.access.data.remote.dto.RegisterCardResponse
import com.gtkim.mobile_access_control.component.access.data.remote.dto.VerifyResponse
import com.gtkim.mobile_access_control.component.access.data.remote.dto.VerifyUserDto
import com.gtkim.mobile_access_control.component.access.domain.model.AccessDecision
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.model.AccessUser
import com.gtkim.mobile_access_control.component.access.domain.model.RegisteredCard
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.DenyReason
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import java.time.Instant

internal fun VerifyResponse.toDomain(): AccessResult = AccessResult(
    decision = AccessDecision.fromWire(result),
    logId = logId,
    user = user.toDomain(),
    denyReason = denyReason?.let(DenyReason::fromWire),
    validUntil = validUntil?.let(Instant::parse),
    verifiedAt = Instant.parse(verifiedAt),
)

private fun VerifyUserDto.toDomain(): AccessUser = AccessUser(
    id = id,
    employeeCode = EmployeeCode(employeeCode),
    name = name,
    department = department,
    photoUrl = photoUrl,
)

internal fun RegisterCardResponse.toDomain(): RegisteredCard = RegisteredCard(
    cardUid = CardUid(cardUid),
    user = user.toDomain(),
)

private fun CardUserDto.toDomain(): AccessUser = AccessUser(
    id = id,
    employeeCode = EmployeeCode(employeeCode),
    name = name,
    department = department,
    // 카드 등록 응답에는 photoUrl 필드가 없다 (API 명세 §4.2).
    photoUrl = null,
)
