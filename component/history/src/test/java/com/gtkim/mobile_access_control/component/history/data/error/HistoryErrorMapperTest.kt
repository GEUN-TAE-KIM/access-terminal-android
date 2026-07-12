package com.gtkim.mobile_access_control.component.history.data.error

import com.gtkim.mobile_access_control.component.history.domain.model.HistoryError
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

class HistoryErrorMapperTest {

    @Test
    fun `transport failures map to typed connectivity errors`() {
        assertEquals(HistoryError.NoConnection, IOException().toHistoryError())
        assertEquals(HistoryError.Timeout, SocketTimeoutException().toHistoryError())
        val cause = RuntimeException("weird")
        assertEquals(HistoryError.Unknown(cause), cause.toHistoryError())
    }

    @Test
    fun `server errorCode maps to the matching history error`() {
        assertServer("REQUEST_VALIDATION_FAILED", HistoryError.ValidationFailed)
        assertServer("REQUEST_MALFORMED_JSON", HistoryError.ValidationFailed)
        assertServer("AUTH_TOKEN_EXPIRED", HistoryError.SessionExpired)
        assertServer("AUTH_TOKEN_INVALID", HistoryError.SessionExpired)
        assertServer("AUTH_TOKEN_MISSING", HistoryError.SessionExpired)
        assertServer("AUTH_FORBIDDEN_ROLE", HistoryError.Forbidden)
    }

    @Test
    fun `unrecognized or missing errorCode falls back to ServerError preserving the code`() {
        assertServer("SOMETHING_NEW", HistoryError.ServerError("SOMETHING_NEW"))
        assertServer(null, HistoryError.ServerError(null))
    }

    private fun assertServer(errorCode: String?, expected: HistoryError) =
        assertEquals(expected, httpError(400, errorCode).toHistoryError())

    private fun httpError(code: Int, errorCode: String?): HttpException {
        val request = Request.Builder()
            .url("http://localhost/api/v1/access/logs")
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
