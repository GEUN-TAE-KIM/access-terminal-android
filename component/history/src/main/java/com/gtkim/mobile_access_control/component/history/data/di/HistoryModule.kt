package com.gtkim.mobile_access_control.component.history.data.di

import com.gtkim.mobile_access_control.component.history.data.HistoryRepositoryImpl
import com.gtkim.mobile_access_control.component.history.data.remote.HistoryApi
import com.gtkim.mobile_access_control.component.history.domain.repository.HistoryRepository
import com.gtkim.mobile_access_control.component.history.domain.usecase.GetAccessLogsUseCase
import com.gtkim.mobile_access_control.component.history.domain.usecase.GetAccessLogsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class HistoryModule {

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    abstract fun bindGetAccessLogsUseCase(impl: GetAccessLogsUseCaseImpl): GetAccessLogsUseCase

    companion object {
        @Provides
        @Singleton
        fun api(retrofit: Retrofit): HistoryApi = retrofit.create(HistoryApi::class.java)
    }
}
