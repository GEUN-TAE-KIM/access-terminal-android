package com.gtkim.mobile_access_control.component.stats.data.di

import com.gtkim.mobile_access_control.component.stats.data.StatsRepositoryImpl
import com.gtkim.mobile_access_control.component.stats.data.remote.StatsApi
import com.gtkim.mobile_access_control.component.stats.domain.repository.StatsRepository
import com.gtkim.mobile_access_control.component.stats.domain.usecase.GetDailyStatsUseCase
import com.gtkim.mobile_access_control.component.stats.domain.usecase.GetDailyStatsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class StatsModule {

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    abstract fun bindGetDailyStatsUseCase(impl: GetDailyStatsUseCaseImpl): GetDailyStatsUseCase

    companion object {
        @Provides
        @Singleton
        fun api(retrofit: Retrofit): StatsApi = retrofit.create(StatsApi::class.java)
    }
}
