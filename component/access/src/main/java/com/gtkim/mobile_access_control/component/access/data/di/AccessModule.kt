package com.gtkim.mobile_access_control.component.access.data.di

import com.gtkim.mobile_access_control.component.access.data.AccessRepositoryImpl
import com.gtkim.mobile_access_control.component.access.data.CardRepositoryImpl
import com.gtkim.mobile_access_control.component.access.data.IdempotencyKeyGeneratorImpl
import com.gtkim.mobile_access_control.component.access.data.LocalAccessVerifierImpl
import com.gtkim.mobile_access_control.component.access.data.remote.AccessApi
import com.gtkim.mobile_access_control.component.access.domain.IdempotencyKeyGenerator
import com.gtkim.mobile_access_control.component.access.domain.repository.AccessRepository
import com.gtkim.mobile_access_control.component.access.domain.repository.CardRepository
import com.gtkim.mobile_access_control.component.access.domain.service.LocalAccessVerifier
import com.gtkim.mobile_access_control.component.access.domain.usecase.RegisterCardUseCase
import com.gtkim.mobile_access_control.component.access.domain.usecase.RegisterCardUseCaseImpl
import com.gtkim.mobile_access_control.component.access.domain.usecase.VerifyAccessUseCase
import com.gtkim.mobile_access_control.component.access.domain.usecase.VerifyAccessUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AccessModule {

    @Binds
    @Singleton
    abstract fun bindAccessRepository(impl: AccessRepositoryImpl): AccessRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds
    @Singleton
    abstract fun bindLocalAccessVerifier(impl: LocalAccessVerifierImpl): LocalAccessVerifier

    @Binds
    abstract fun bindIdempotencyKeyGenerator(impl: IdempotencyKeyGeneratorImpl): IdempotencyKeyGenerator

    @Binds
    abstract fun bindVerifyAccessUseCase(impl: VerifyAccessUseCaseImpl): VerifyAccessUseCase

    @Binds
    abstract fun bindRegisterCardUseCase(impl: RegisterCardUseCaseImpl): RegisterCardUseCase

    companion object {
        // 인증이 필요한 메인 Retrofit(qualifier 없음 — TraceId→AppVersion→Auth→ProblemDetail→Logging
        // 파이프라인). verify 는 Idempotency-Key 를 @Header 로 호출자(검문 세션)가 직접 넘긴다
        // (architecture.md §2 — 재시도 시 동일 키 재사용을 보장하기 위해 인터셉터로 자동화하지 않음).
        @Provides
        @Singleton
        fun accessApi(retrofit: Retrofit): AccessApi = retrofit.create(AccessApi::class.java)
    }
}
