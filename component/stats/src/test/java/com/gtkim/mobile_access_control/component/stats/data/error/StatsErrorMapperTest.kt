package com.gtkim.mobile_access_control.component.stats.data.error

import com.gtkim.mobile_access_control.component.stats.domain.model.StatsError
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

class StatsErrorMapperTest {

    @Test
    fun `transport failures map to typed connectivity errors`() {
        assertEquals(StatsError.NoConnection, IOException().toStatsError())
        assertEquals(StatsError.Timeout, SocketTimeoutException().toStatsError())
        val cause = RuntimeException("weird")
        assertEquals(StatsError.Unknown(cause), cause.toStatsError())
    }

    @Test
    fun `server errorCode maps to the matching stats error`() {
        assertServer("REQUEST_VALIDATION_FAILED", StatsError.ValidationFailed)
        assertServer("AUTH_TOKEN_EXPIRED", StatsError.SessionExpired)
        assertServer("AUTH_TOKEN_INVALID", StatsError.SessionExpired)
        assertServer("AUTH_TOKEN_MISSING", StatsError.SessionExpired)
        assertServer("AUTH_FORBIDDEN_ROLE", StatsError.Forbidden)
    }

    @Test
    fun `unrecognized or missing errorCode falls back to ServerError preserving the code`() {
        assertServer("SOMETHING_NEW", StatsError.ServerError("SOMETHING_NEW"))
        assertServer(null, StatsError.ServerError(null))
    }

    private fun assertServer(errorCode: String?, expected: StatsError) =
        assertEquals(expected, httpError(400, errorCode).toStatsError())

    private fun httpError(code: Int, errorCode: String?): HttpException {
        val request = Request.Builder()
            .url("http://localhost/api/v1/access/stats")
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
