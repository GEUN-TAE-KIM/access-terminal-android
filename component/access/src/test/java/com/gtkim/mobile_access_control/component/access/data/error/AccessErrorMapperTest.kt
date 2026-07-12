package com.gtkim.mobile_access_control.component.access.data.error

import com.gtkim.mobile_access_control.component.access.domain.model.AccessError
import com.gtkim.mobile_access_control.core.network.error.ProblemDetail
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response as OkHttpResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response as RetrofitResponse
import java.io.IOException
import java.net.SocketTimeoutException

class AccessErrorMapperTest {

    @Test
    fun `transport failures map to typed connectivity errors`() {
        assertEquals(AccessError.NoConnection, IOException().toAccessError())
        assertEquals(AccessError.Timeout, SocketTimeoutException().toAccessError())
        val cause = RuntimeException("weird")
        assertEquals(AccessError.Unknown(cause), cause.toAccessError())
    }

    @Test
    fun `server errorCode maps to the matching business error`() {
        assertServer("ACCESS_CARD_NOT_REGISTERED", AccessError.CardNotRegistered)
        assertServer("ACCESS_REPLAY_DETECTED", AccessError.ReplayDetected)
        assertServer("ACCESS_TIMESTAMP_SKEW", AccessError.TimestampSkew)
        assertServer("ACCESS_IDEMPOTENCY_CONFLICT", AccessError.IdempotencyConflict)
        assertServer("ACCESS_INVALID_CARD_TYPE", AccessError.InvalidCardType)
        assertServer("REQUEST_VALIDATION_FAILED", AccessError.ValidationFailed)
        assertServer("REQUEST_MALFORMED_JSON", AccessError.ValidationFailed)
        assertServer("REQUEST_MISSING_HEADER", AccessError.MissingHeader)
        assertServer("AUTH_TOKEN_EXPIRED", AccessError.SessionExpired)
        assertServer("AUTH_TOKEN_INVALID", AccessError.SessionExpired)
        assertServer("AUTH_TOKEN_MISSING", AccessError.SessionExpired)
        assertServer("RATE_LIMIT_EXCEEDED", AccessError.RateLimited)
    }

    @Test
    fun `unrecognized or missing errorCode falls back to ServerError preserving the code`() {
        assertServer("SOMETHING_NEW", AccessError.ServerError("SOMETHING_NEW"))
        assertServer(null, AccessError.ServerError(null))
    }

    private fun assertServer(errorCode: String?, expected: AccessError) =
        assertEquals(expected, httpError(400, errorCode).toAccessError())

    private fun httpError(code: Int, errorCode: String?): HttpException {
        val request = Request.Builder()
            .url("http://localhost/api/v1/access/verify")
            .tag(ProblemDetail::class.java, ProblemDetail(errorCode = errorCode))
            .build()
        val raw = OkHttpResponse.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("error")
            .body("".toResponseBody(null))
            .build()
        return HttpException(RetrofitResponse.error<Any>("{}".toResponseBody(null), raw))
    }
}
