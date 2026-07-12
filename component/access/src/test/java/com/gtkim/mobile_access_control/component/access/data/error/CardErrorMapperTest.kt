package com.gtkim.mobile_access_control.component.access.data.error

import com.gtkim.mobile_access_control.component.access.domain.model.CardError
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

class CardErrorMapperTest {

    @Test
    fun `transport failures map to typed connectivity errors`() {
        assertEquals(CardError.NoConnection, IOException().toCardError())
        assertEquals(CardError.Timeout, SocketTimeoutException().toCardError())
        val cause = RuntimeException("weird")
        assertEquals(CardError.Unknown(cause), cause.toCardError())
    }

    @Test
    fun `server errorCode maps to the matching registration error`() {
        assertServer("USER_NOT_FOUND", CardError.UserNotFound)
        assertServer("CARD_ALREADY_REGISTERED", CardError.CardAlreadyRegistered)
        assertServer("USER_INACTIVE", CardError.UserInactive)
        assertServer("AUTH_FORBIDDEN_ROLE", CardError.ForbiddenRole)
        assertServer("REQUEST_VALIDATION_FAILED", CardError.ValidationFailed)
        assertServer("REQUEST_MALFORMED_JSON", CardError.ValidationFailed)
        assertServer("AUTH_TOKEN_EXPIRED", CardError.SessionExpired)
        assertServer("AUTH_TOKEN_INVALID", CardError.SessionExpired)
        assertServer("AUTH_TOKEN_MISSING", CardError.SessionExpired)
        assertServer("RATE_LIMIT_EXCEEDED", CardError.RateLimited)
    }

    @Test
    fun `unrecognized or missing errorCode falls back to ServerError preserving the code`() {
        assertServer("SOMETHING_NEW", CardError.ServerError("SOMETHING_NEW"))
        assertServer(null, CardError.ServerError(null))
    }

    private fun assertServer(errorCode: String?, expected: CardError) =
        assertEquals(expected, httpError(400, errorCode).toCardError())

    private fun httpError(code: Int, errorCode: String?): HttpException {
        val request = Request.Builder()
            .url("http://localhost/api/v1/access/cards")
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
