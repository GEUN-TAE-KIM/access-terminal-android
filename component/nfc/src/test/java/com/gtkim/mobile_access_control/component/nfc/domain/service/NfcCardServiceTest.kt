package com.gtkim.mobile_access_control.component.nfc.domain.service

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardData
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.nfc.domain.reader.NfcTagReader
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NfcCardServiceTest {

    private val tag = mockk<Tag>()

    @Test
    fun `delegates to the first reader whose supports returns true`() = runTest {
        val expected = Outcome.Success(card(CardType.FELICA))
        val unsupported = mockk<NfcTagReader> { every { supports(tag) } returns false }
        val supported = mockk<NfcTagReader> {
            every { supports(tag) } returns true
            coEvery { read(tag) } returns expected
        }
        // LinkedHashSet → 결정적 순회 순서로 "첫 supports() 매칭" 전략을 검증.
        val service = NfcCardService(linkedSetOf(unsupported, supported))

        val result = service.read(tag)

        assertEquals(expected, result)
        coVerify(exactly = 0) { unsupported.read(any()) }
        coVerify(exactly = 1) { supported.read(tag) }
    }

    @Test
    fun `picks the first matching reader when several support the tag`() = runTest {
        val first = mockk<NfcTagReader> {
            every { supports(tag) } returns true
            coEvery { read(tag) } returns Outcome.Success(card(CardType.FELICA))
        }
        val second = mockk<NfcTagReader> { every { supports(tag) } returns true }
        val service = NfcCardService(linkedSetOf(first, second))

        service.read(tag)

        coVerify(exactly = 1) { first.read(tag) }
        coVerify(exactly = 0) { second.read(any()) }
    }

    @Test
    fun `returns UnreadableTag when no reader supports the tag`() = runTest {
        val reader = mockk<NfcTagReader> { every { supports(tag) } returns false }
        val service = NfcCardService(setOf(reader))

        val result = service.read(tag)

        assertEquals(Outcome.Failure(NfcError.UnreadableTag), result)
        coVerify(exactly = 0) { reader.read(any()) }
    }

    private fun card(type: CardType) = CardData(uid = CardUid("0123456789ABCDEF"), type = type, payload = ByteArray(0))
}
