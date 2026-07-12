package com.gtkim.mobile_access_control.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ValueClassTest {

    @Test
    fun `id value classes keep their raw value`() {
        assertEquals("0123456789ABCDEF", CardUid("0123456789ABCDEF").value)
        assertEquals("EMP001", EmployeeCode("EMP001").value)
        assertEquals("GATE-A", Zone("GATE-A").value)
        assertEquals("MOBILE-001", TerminalId("MOBILE-001").value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `CardUid rejects blank`() {
        CardUid("  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `EmployeeCode rejects blank`() {
        EmployeeCode("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Zone rejects blank`() {
        Zone(" ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `TerminalId rejects blank`() {
        TerminalId("")
    }

    @Test
    fun `TraceId new generates a fresh UUID string each call`() {
        val a = TraceId.new()
        val b = TraceId.new()

        assertEquals(36, a.value.length)
        assertNotEquals(a.value, b.value)
    }
}
