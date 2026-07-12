package com.gtkim.mobile_access_control.core.network.error

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Retrofit/OkHttp 가 던지는 [Throwable] 을 [NetworkError] 로 분류한다.
 *
 * 각 `:component` 의 data 레이어 에러 매퍼는 이 결과를 받아 [NetworkError.Server] 의
 * [ProblemDetail.errorCode] 로 자기 도메인 에러를 결정한다 (3-layer error mapping).
 *
 * [ProblemDetail] 은 [ProblemDetailParser] 가 에러 응답의 request tag 에 미리 심어둔 것을 꺼낸다.
 */
fun Throwable.toNetworkError(): NetworkError = when (this) {
    is NetworkError -> this
    is HttpException -> NetworkError.Server(code(), problemDetail())
    is SocketTimeoutException -> NetworkError.Timeout
    is IOException -> NetworkError.NoConnection
    else -> NetworkError.Unknown(this)
}

private fun HttpException.problemDetail(): ProblemDetail? =
    response()?.raw()?.request?.tag(ProblemDetail::class.java)
