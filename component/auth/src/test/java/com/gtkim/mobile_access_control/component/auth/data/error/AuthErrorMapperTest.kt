package com.gtkim.mobile_access_control.component.auth.data.error

import com.gtkim.mobile_access_control.component.auth.domain.model.AuthError
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

class AuthErrorMapperTest {

    @Test
    fun `transport failures map to typed connectivity errors`() {
        assertEquals(AuthError.NoConnection, IOException().toAuthError())
        assertEquals(AuthError.Timeout, SocketTimeoutException().toAuthError())
        val cause = RuntimeException("weird")
        assertEquals(AuthError.Unknown(cause), cause.toAuthError())
    }

    @Test
    fun `server errorCode maps to the matching auth error`() {
        assertServer("AUTH_INVALID_CREDENTIALS", AuthError.InvalidCredentials)
        assertServer("RATE_LIMIT_EXCEEDED", AuthError.RateLimited)
        assertServer("REQUEST_VALIDATION_FAILED", AuthError.ValidationFailed)
        assertServer("REQUEST_MALFORMED_JSON", AuthError.ValidationFailed)
        assertServer("AUTH_TOKEN_MISSING", AuthError.SessionExpired)
        assertServer("AUTH_TOKEN_INVALID", AuthError.SessionExpired)
        assertServer("AUTH_REFRESH_EXPIRED", AuthError.SessionExpired)
        assertServer("AUTH_REFRESH_REVOKED", AuthError.SessionExpired)
    }

    @Test
    fun `unrecognized or missing errorCode falls back to ServerError preserving the code`() {
        assertServer("SOMETHING_NEW", AuthError.ServerError("SOMETHING_NEW"))
        assertServer(null, AuthError.ServerError(null))
    }

    private fun assertServer(errorCode: String?, expected: AuthError) =
        assertEquals(expected, httpError(401, errorCode).toAuthError())

    private fun httpError(code: Int, errorCode: String?): HttpException {
        val request = Request.Builder()
            .url("http://localhost/api/v1/auth/login")
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
