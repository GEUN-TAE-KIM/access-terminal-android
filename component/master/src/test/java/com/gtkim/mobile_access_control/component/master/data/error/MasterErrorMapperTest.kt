package com.gtkim.mobile_access_control.component.master.data.error

import com.gtkim.mobile_access_control.component.master.domain.model.MasterError
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

class MasterErrorMapperTest {

    @Test
    fun `transport failures map to typed connectivity errors`() {
        assertEquals(MasterError.NoConnection, IOException().toMasterError())
        assertEquals(MasterError.Timeout, SocketTimeoutException().toMasterError())
        val cause = RuntimeException("weird")
        assertEquals(MasterError.Unknown(cause), cause.toMasterError())
    }

    @Test
    fun `server responses preserve status code and errorCode`() {
        assertEquals(MasterError.ServerError(500, "SERVER_INTERNAL_ERROR"), httpError(500, "SERVER_INTERNAL_ERROR").toMasterError())
        assertEquals(MasterError.ServerError(401, "AUTH_TOKEN_EXPIRED"), httpError(401, "AUTH_TOKEN_EXPIRED").toMasterError())
        assertEquals(MasterError.ServerError(400, null), httpError(400, null).toMasterError())
    }

    private fun httpError(code: Int, errorCode: String?): HttpException {
        val request = Request.Builder()
            .url("http://localhost/api/v1/master/snapshot")
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
