package com.gtkim.mobile_access_control.component.access.data

import com.gtkim.mobile_access_control.component.access.domain.model.AccessDecision
import com.gtkim.mobile_access_control.component.access.domain.model.AccessError
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.model.AccessUser
import com.gtkim.mobile_access_control.core.model.DenyReason
import com.gtkim.mobile_access_control.component.access.domain.service.LocalAccessVerifier
import com.gtkim.mobile_access_control.component.master.domain.model.CachedPermission
import com.gtkim.mobile_access_control.component.master.domain.repository.MasterDataRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject

/**
 * server `PermissionEvaluator` (access-terminal-server) 와 1:1 매칭. 같은 입력 → 같은 enum.
 * golden test matrix (docs/hybrid-offline.md §2.3) 로 동치 검증.
 *
 * `name` / `department` 는 master 캐시에 없으므로 빈 값으로 채운다 — UI 는 빈 문자열일 때
 * employeeCode 로 fallback (hybrid-offline.md §2.1).
 */
internal class LocalAccessVerifierImpl @Inject constructor(
    private val masterData: MasterDataRepository,
    private val time: TimeProvider,
) : LocalAccessVerifier {

    override suspend fun verify(cardUid: CardUid, zone: Zone): Outcome<AccessResult, AccessError> {
        val card = masterData.cardByUid(cardUid)
            ?: return Outcome.Failure(AccessError.CardNotRegistered)
        val user = masterData.userById(card.userId)
            ?: return Outcome.Failure(AccessError.CardNotRegistered)

        val now = time.now()
        val accessUser = AccessUser(
            id = user.id,
            employeeCode = user.employeeCode,
            // master 캐시는 PII 미보관 — UI 는 빈 문자열일 때 employeeCode 로 fallback.
            name = "",
            department = null,
            photoUrl = null,
        )

        // 1. card 비활성
        if (!card.isActive) return success(AccessDecision.DENIED_INACTIVE_CARD, accessUser, now, denyReason = DenyReason.CARD_REVOKED)
        // 2. user 비활성
        if (!user.isActive) return success(AccessDecision.DENIED_INACTIVE_USER, accessUser, now, denyReason = DenyReason.USER_INACTIVE)

        // 3. zone 권한 없음
        val permissions = masterData.permissionsForUserAndZone(user.id, zone)
        if (permissions.isEmpty()) return success(AccessDecision.DENIED_NO_PERMISSION, accessUser, now, denyReason = DenyReason.NO_PERMISSION_FOR_ZONE)

        // 4. 유효기간 통과
        val withinPeriod = permissions.filter { it.isWithinValidPeriod(now) }
        if (withinPeriod.isEmpty()) return success(AccessDecision.DENIED_EXPIRED, accessUser, now, denyReason = DenyReason.PERMISSION_EXPIRED)

        // 5. JST 허용 시간대 통과
        val localTime = now.atZone(time.zoneId()).toLocalTime()
        val granting = withinPeriod.filter { it.isWithinAllowedHours(localTime) }
        if (granting.isEmpty()) return success(AccessDecision.DENIED_OUT_OF_HOURS, accessUser, now, denyReason = DenyReason.OUT_OF_ALLOWED_HOURS)

        // 6. 허용 — 가장 관대한 validUntil (null 우선)
        return success(AccessDecision.ALLOWED, accessUser, now, validUntil = pickValidUntil(granting), denyReason = null)
    }

    private fun success(
        decision: AccessDecision,
        user: AccessUser,
        now: Instant,
        denyReason: DenyReason?,
        validUntil: Instant? = null,
    ): Outcome.Success<AccessResult> = Outcome.Success(
        AccessResult(
            decision = decision,
            // offline 판정은 server access_logs.id 가 없다. 0 은 "server 미할당" sentinel —
            // audit batch upload 후 server 가 발급한 logId 가 응답으로 돌아오지만 (api-spec §5.2),
            // 그 시점엔 검문 결과 화면이 이미 사라진 뒤라 클라이언트가 활용할 일이 없다.
            logId = 0L,
            user = user,
            denyReason = denyReason,
            validUntil = validUntil,
            verifiedAt = now,
        )
    )

    private fun CachedPermission.isWithinValidPeriod(now: Instant): Boolean {
        val until = validUntil ?: return true
        return now.isBefore(until)
    }

    private fun CachedPermission.isWithinAllowedHours(localTime: LocalTime): Boolean {
        if (isAllDay) return true
        // 한쪽만 null(서버 명세상 미발생)이어도 시간 제한 없음으로 안전 처리 — `!!` 대신 null-guard
        // (architecture.md §16, 위 isWithinValidPeriod 와 동일 스타일).
        val start = allowedHoursStart ?: return true
        val end = allowedHoursEnd ?: return true
        return !localTime.isBefore(start) && localTime.isBefore(end)
    }

    private fun pickValidUntil(granting: List<CachedPermission>): Instant? {
        if (granting.any { it.validUntil == null }) return null
        return granting.mapNotNull { it.validUntil }.maxOrNull()
    }
}
