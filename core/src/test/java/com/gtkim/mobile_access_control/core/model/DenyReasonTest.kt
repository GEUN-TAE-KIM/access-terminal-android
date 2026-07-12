package com.gtkim.mobile_access_control.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class DenyReasonTest {

    @Test
    fun `fromWire maps every known wire code`() {
        assertEquals(DenyReason.NO_PERMISSION_FOR_ZONE, DenyReason.fromWire("ACCESS_DENIED_NO_PERMISSION_FOR_ZONE"))
        assertEquals(DenyReason.OUT_OF_ALLOWED_HOURS, DenyReason.fromWire("ACCESS_DENIED_OUT_OF_ALLOWED_HOURS"))
        assertEquals(DenyReason.PERMISSION_EXPIRED, DenyReason.fromWire("ACCESS_DENIED_PERMISSION_EXPIRED"))
        assertEquals(DenyReason.CARD_REVOKED, DenyReason.fromWire("ACCESS_DENIED_CARD_REVOKED"))
        assertEquals(DenyReason.USER_INACTIVE, DenyReason.fromWire("ACCESS_DENIED_USER_INACTIVE"))
        assertEquals(DenyReason.CARD_NOT_REGISTERED, DenyReason.fromWire("ACCESS_CARD_NOT_REGISTERED"))
    }

    @Test
    fun `fromWire falls back to UNKNOWN for unrecognized codes (forward-compat)`() {
        assertEquals(DenyReason.UNKNOWN, DenyReason.fromWire("SOME_FUTURE_CODE"))
        assertEquals(DenyReason.UNKNOWN, DenyReason.fromWire(""))
    }

    @Test
    fun `toWire then fromWire round-trips for every selectable reason`() {
        DenyReason.selectable.forEach { reason ->
            val wire = reason.toWire()
            assertEquals("toWire must be non-null for selectable $reason", true, wire != null)
            assertEquals(reason, DenyReason.fromWire(wire!!))
        }
    }

    @Test
    fun `UNKNOWN serializes to null (not client-selectable)`() {
        assertNull(DenyReason.UNKNOWN.toWire())
    }

    @Test
    fun `selectable excludes UNKNOWN`() {
        assertFalse(DenyReason.UNKNOWN in DenyReason.selectable)
        assertEquals(DenyReason.entries.size - 1, DenyReason.selectable.size)
    }
}
