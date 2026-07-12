package com.gtkim.mobile_access_control.component.scan.data

import com.gtkim.mobile_access_control.component.access.domain.model.CardType as AccessCardType
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType as NfcCardType
import org.junit.Assert.assertEquals
import org.junit.Test

class CardTypeMapperTest {

    @Test
    fun `FELICA 는 access FELICA 로 매핑`() {
        assertEquals(AccessCardType.FELICA, NfcCardType.FELICA.toAccessCardType())
    }

    @Test
    fun `ISO_DEP 는 access ISO_DEP 로 매핑`() {
        assertEquals(AccessCardType.ISO_DEP, NfcCardType.ISO_DEP.toAccessCardType())
    }

    @Test
    fun `MOCK 은 access MOCK 으로 매핑`() {
        assertEquals(AccessCardType.MOCK, NfcCardType.MOCK.toAccessCardType())
    }
}
