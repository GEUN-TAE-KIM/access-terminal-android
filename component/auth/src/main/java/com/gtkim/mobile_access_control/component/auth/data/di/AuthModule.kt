package com.gtkim.mobile_access_control.component.auth.data.di

import com.gtkim.mobile_access_control.component.auth.data.AuthRepositoryImpl
import com.gtkim.mobile_access_control.component.auth.data.remote.AuthApi
import com.gtkim.mobile_access_control.component.auth.data.token.AuthTokenProvider
import com.gtkim.mobile_access_control.component.auth.domain.repository.AuthRepository
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LoginUseCase
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LoginUseCaseImpl
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCaseImpl
import com.gtkim.mobile_access_control.core.network.auth.TokenProvider
import com.gtkim.mobile_access_control.core.network.di.AuthApiClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: AuthTokenProvider): TokenProvider

    @Binds
    abstract fun bindLoginUseCase(impl: LoginUseCaseImpl): LoginUseCase

    @Binds
    abstract fun bindLogoutUseCase(impl: LogoutUseCaseImpl): LogoutUseCase

    companion object {
        @Provides
        @Singleton
        fun authApi(@AuthApiClient retrofit: Retrofit): AuthApi =
            retrofit.create(AuthApi::class.java)
    }
}
