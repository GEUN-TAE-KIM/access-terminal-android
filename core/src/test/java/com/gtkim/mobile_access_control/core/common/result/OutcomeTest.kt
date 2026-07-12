package com.gtkim.mobile_access_control.core.common.result

import com.gtkim.mobile_access_control.core.common.error.UiText
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class OutcomeTest {

    private data class TestError(override val message: UiText = UiText.Raw("test")) : AppError

    @Test
    fun `safeCall wraps a successful block in Success`() {
        val result = safeCall({ TestError() }) { 42 }

        assertEquals(Outcome.Success(42), result)
    }

    @Test
    fun `safeCall maps a thrown exception to Failure via onError`() {
        val error = TestError()

        val result = safeCall({ error }) { throw IllegalStateException("boom") }

        assertEquals(Outcome.Failure(error), result)
    }

    @Test
    fun `safeCall rethrows CancellationException instead of mapping it`() {
        var onErrorCalled = false

        try {
            safeCall({ onErrorCalled = true; TestError() }) {
                throw CancellationException("cancelled")
            }
            fail("expected CancellationException to propagate")
        } catch (e: CancellationException) {
            // expected — 코루틴 취소 시그널을 비즈니스 실패로 둔갑시키지 않는다.
        }

        assertEquals(false, onErrorCalled)
    }
}
