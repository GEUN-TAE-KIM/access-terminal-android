package com.gtkim.mobile_access_control.di

import com.gtkim.mobile_access_control.core.common.config.AppConfig
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.network.RetrofitFactory
import com.gtkim.mobile_access_control.core.network.auth.TokenProvider
import com.gtkim.mobile_access_control.core.network.di.AuthApiClient
import com.gtkim.mobile_access_control.core.network.interceptor.AcceptHeaderInterceptor
import com.gtkim.mobile_access_control.core.network.interceptor.AuthInterceptor
import com.gtkim.mobile_access_control.core.network.interceptor.ProblemDetailParser
import com.gtkim.mobile_access_control.core.network.interceptor.TraceIdInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    // --- Auth 전용 client (AuthApi: login/refresh) -------------------------------
    // AuthInterceptor 가 빠져 있다 — refresh 호출이 메인 client 를 타면 401 → refresh →
    // 401 무한 재귀가 되므로 구조적으로 분리한다. Idempotency 도 auth 엔드포인트엔 불필요.

    @Provides
    @Singleton
    @AuthApiClient
    fun authOkHttpClient(
        config: AppConfig,
        json: Json,
    ): OkHttpClient = OkHttpClient.Builder()
        // api-spec §3 SLA: login 5s / refresh·logout 3s. callTimeout 으로 worst-case cap,
        // connect/read/write 는 OkHttp 기본(10s)보다 타이트하게 — 모바일 환경 응답성 우선.
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(TraceIdInterceptor())
        .addInterceptor(AcceptHeaderInterceptor())
        .addInterceptor(ProblemDetailParser(json))
        .addInterceptor(loggingInterceptor(config))
        .build()

    @Provides
    @Singleton
    @AuthApiClient
    fun authRetrofit(
        config: AppConfig,
        @AuthApiClient client: OkHttpClient,
        json: Json,
    ): Retrofit = RetrofitFactory.create(config.baseUrl, client, json)

    // --- 메인 client (인증이 필요한 그 외 모든 API) ------------------------------

    @Provides
    @Singleton
    fun authInterceptor(tokenProvider: TokenProvider, time: TimeProvider): AuthInterceptor =
        AuthInterceptor(tokenProvider, time)

    /**
     * OkHttp pipeline 순서 고정:
     * TraceId → Accept → Auth → ProblemDetail → Logging
     *
     * `Idempotency-Key` 는 인터셉터로 자동 부여하지 않는다 — verify 호출자([AccessApi.verify])가
     * 직접 `@Header` 인자로 키를 전달한다. 검문 세션 단위로 호출자가 키를 보관·재사용하는 정책
     * (architecture.md §2 "같은 비즈니스 요청에 동일 키 유지"). 인터셉터에서 새 UUID 를 매번 발급하면
     * 재시도 호출이 매번 새 키가 되어 멱등성이 깨지므로 의도적으로 호출자 책임으로 둔다.
     */
    @Provides
    @Singleton
    fun okHttpClient(
        config: AppConfig,
        json: Json,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        // api-spec SLA: verify·cards 3s / logs 5s / stats·master 10s / batch 30s.
        // callTimeout 은 batch 의 30s SLA 에 retry/refresh 여유까지 더해 cap.
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(35, TimeUnit.SECONDS)
        .addInterceptor(TraceIdInterceptor())
        .addInterceptor(AcceptHeaderInterceptor())
        .addInterceptor(authInterceptor)
        .addInterceptor(ProblemDetailParser(json))
        .addInterceptor(loggingInterceptor(config))
        .build()

    @Provides
    @Singleton
    fun retrofit(
        config: AppConfig,
        client: OkHttpClient,
        json: Json,
    ): Retrofit = RetrofitFactory.create(config.baseUrl, client, json)

    private fun loggingInterceptor(config: AppConfig): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (config.isDebug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
}
