package com.gtkim.mobile_access_control.core.network.interceptor

import com.gtkim.mobile_access_control.core.network.error.ProblemDetail
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response

/**
 * RFC 7807 application/problem+json 응답을 파싱하여 ProblemDetail 로 변환한다.
 * 호출부에서 response.body 를 읽기 전에 동작.
 */
class ProblemDetailParser(
    private val json: Json,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) {
            val contentType = response.header("Content-Type").orEmpty()
            if ("problem+json" in contentType) {
                val body = response.peekBody(MAX_PEEK).string()
                val problem = runCatching { json.decodeFromString<ProblemDetail>(body) }.getOrNull()
                if (problem != null) {
                    return response.newBuilder()
                        .request(chain.request().newBuilder().tag(ProblemDetail::class.java, problem).build())
                        .build()
                }
            }
        }
        return response
    }

    companion object {
        private const val MAX_PEEK = 1L shl 20
    }
}
