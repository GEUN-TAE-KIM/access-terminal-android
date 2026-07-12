package com.gtkim.mobile_access_control.component.scan.data

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.access.domain.model.AccessUser
import com.gtkim.mobile_access_control.component.access.domain.model.RegisteredCard
import com.gtkim.mobile_access_control.component.access.domain.usecase.RegisterCardUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardData
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.nfc.domain.usecase.ReadNfcCardUseCase
import com.gtkim.mobile_access_control.component.scan.domain.usecase.RegisterScannedCardUseCaseImpl
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.component.access.domain.model.CardType as AccessCardType
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType as NfcCardType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterScannedCardUseCaseImplTest {

    private val tag = mockk<Tag>()
    private val readNfcCard = mockk<ReadNfcCardUseCase>()
    private val registerCard = mockk<RegisterCardUseCase>()

    private val useCase = RegisterScannedCardUseCaseImpl(readNfcCard, registerCard)

    @Test
    fun `read 성공 시 매핑된 cardType 으로 register 위임하고 결과 전파`() = runTest {
        coEvery { readNfcCard(tag) } returns Outcome.Success(
            CardData(uid = CardUid(UID), type = NfcCardType.ISO_DEP, payload = ByteArray(0)),
        )
        val expected = Outcome.Success(registered)
        coEvery { registerCard(CardUid(UID), AccessCardType.ISO_DEP, EMP) } returns expected

        val result = useCase(tag, EMP)

        assertEquals(expected, result)
        coVerify(exactly = 1) { registerCard(CardUid(UID), AccessCardType.ISO_DEP, EMP) }
    }

    @Test
    fun `read 실패 시 register 를 호출하지 않고 실패를 단락 전파`() = runTest {
        val failure = Outcome.Failure(NfcError.TagLost)
        coEvery { readNfcCard(tag) } returns failure

        val result = useCase(tag, EMP)

        assertEquals(failure, result)
        coVerify(exactly = 0) { registerCard(any(), any(), any()) }
    }

    private companion object {
        const val UID = "0123456789ABCDEF"
        val EMP = EmployeeCode("EMP001")

        val registered = RegisteredCard(
            cardUid = CardUid(UID),
            user = AccessUser(
                id = 1L,
                employeeCode = EMP,
                name = "山田太郎",
                department = "建設部",
                photoUrl = null,
            ),
        )
    }
}
