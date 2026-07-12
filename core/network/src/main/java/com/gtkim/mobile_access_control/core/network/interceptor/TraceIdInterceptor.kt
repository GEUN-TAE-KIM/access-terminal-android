package com.gtkim.mobile_access_control.core.network.interceptor

import com.gtkim.mobile_access_control.core.model.TraceId
import okhttp3.Interceptor
import okhttp3.Response

class TraceIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(HEADER, TraceId.new().value)
            .build()
        return chain.proceed(request)
    }

    companion object {
        const val HEADER = "X-Trace-Id"
    }
}
