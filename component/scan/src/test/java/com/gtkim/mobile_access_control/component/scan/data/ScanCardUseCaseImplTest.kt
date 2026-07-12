package com.gtkim.mobile_access_control.component.scan.data

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.access.domain.IdempotencyKeyGenerator
import com.gtkim.mobile_access_control.component.access.domain.model.AccessDecision
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.model.AccessUser
import com.gtkim.mobile_access_control.component.access.domain.usecase.VerifyAccessUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardData
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.nfc.domain.usecase.ReadNfcCardUseCase
import com.gtkim.mobile_access_control.component.scan.domain.usecase.ScanCardUseCaseImpl
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

class ScanCardUseCaseImplTest {

    private val tag = mockk<Tag>()
    private val readNfcCard = mockk<ReadNfcCardUseCase>()
    private val verifyAccess = mockk<VerifyAccessUseCase>()
    private val idempotencyKeyGenerator = mockk<IdempotencyKeyGenerator> {
        every { newKey() } returns KEY
    }

    private val useCase = ScanCardUseCaseImpl(readNfcCard, verifyAccess, idempotencyKeyGenerator)

    @Test
    fun `read 성공 시 매핑된 cardType·zone·idempotencyKey 로 verify 위임하고 결과 전파`() = runTest {
        coEvery { readNfcCard(tag) } returns Outcome.Success(
            CardData(uid = CardUid(UID), type = NfcCardType.FELICA, payload = ByteArray(0)),
        )
        val expected = Outcome.Success(allowed)
        coEvery { verifyAccess(CardUid(UID), AccessCardType.FELICA, ZONE, KEY) } returns expected

        val result = useCase(tag, ZONE)

        assertEquals(expected, result)
        coVerify(exactly = 1) { verifyAccess(CardUid(UID), AccessCardType.FELICA, ZONE, KEY) }
    }

    @Test
    fun `read 실패 시 verify 를 호출하지 않고 실패를 단락 전파`() = runTest {
        val failure = Outcome.Failure(NfcError.UnreadableTag)
        coEvery { readNfcCard(tag) } returns failure

        val result = useCase(tag, ZONE)

        assertEquals(failure, result)
        coVerify(exactly = 0) { verifyAccess(any(), any(), any(), any()) }
    }

    private companion object {
        const val UID = "0123456789ABCDEF"
        const val KEY = "idem-key-123"
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
