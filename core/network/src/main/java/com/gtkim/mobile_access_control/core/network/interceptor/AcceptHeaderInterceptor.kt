package com.gtkim.mobile_access_control.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 모든 요청에 `Accept: application/json` 을 부착. 에러 응답이 `application/problem+json` 이라도
 * Spring 의 컨텐츠 협상은 application/json 을 만족하므로 충돌 없다 (api-spec.md §2.1).
 */
class AcceptHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
