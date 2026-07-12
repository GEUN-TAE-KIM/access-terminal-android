package com.gtkim.mobile_access_control.component.scan.data

import com.gtkim.mobile_access_control.component.access.domain.IdempotencyKeyGenerator
import com.gtkim.mobile_access_control.component.access.domain.model.AccessDecision
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.model.AccessUser
import com.gtkim.mobile_access_control.component.access.domain.usecase.VerifyAccessUseCase
import com.gtkim.mobile_access_control.component.scan.domain.usecase.VerifyScannedCardUseCaseImpl
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.core.model.Zone
import com.gtkim.mobile_access_control.component.access.domain.model.CardType as AccessCardType
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType as NfcCardType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class VerifyScannedCardUseCaseImplTest {

    private val verifyAccess = mockk<VerifyAccessUseCase>()
    private val idempotencyKeyGenerator = mockk<IdempotencyKeyGenerator> {
        every { newKey() } returns KEY
    }

    private val useCase = VerifyScannedCardUseCaseImpl(verifyAccess, idempotencyKeyGenerator)

    @Test
    fun `nfc cardType 을 access cardType 으로 매핑해 verify 에 위임 (read 단계 없음)`() = runTest {
        val expected = Outcome.Success(allowed)
        coEvery { verifyAccess(CardUid("EMP001"), AccessCardType.MOCK, ZONE, KEY) } returns expected

        val result = useCase(CardUid("EMP001"), NfcCardType.MOCK, ZONE)

        assertEquals(expected, result)
        coVerify(exactly = 1) { verifyAccess(CardUid("EMP001"), AccessCardType.MOCK, ZONE, KEY) }
    }

    private companion object {
        const val KEY = "idem-key-xyz"
        val ZONE = Zone("GATE-A")

        val allowed = AccessResult(
            decision = AccessDecision.ALLOWED,
            logId = 1L,
            user = AccessUser(
                id = 1L,
                employeeCode = EmployeeCode("EMP001"),
                name = "山田太郎",
                department = "建設部",
                photoUrl = null,
            ),
            denyReason = null,
            validUntil = null,
            verifiedAt = Instant.EPOCH,
        )
    }
}
