package com.gtkim.mobile_access_control.core.network.di

import javax.inject.Qualifier

/**
 * `AuthApi`(login/refresh) 전용 OkHttpClient/Retrofit 을 메인 것과 구분하는 qualifier.
 *
 * auth 전용 client 는 [com.gtkim.mobile_access_control.core.network.interceptor.AuthInterceptor]
 * 를 포함하지 않는다 — refresh 호출이 메인 client 를 타면 401 → refresh → 401 무한 재귀가
 * 되므로 구조적으로 분리한다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthApiClient
