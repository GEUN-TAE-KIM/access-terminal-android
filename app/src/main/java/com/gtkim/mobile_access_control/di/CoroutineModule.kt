package com.gtkim.mobile_access_control.di

import com.gtkim.mobile_access_control.core.di.qualifier.AppScope
import com.gtkim.mobile_access_control.core.di.qualifier.DefaultDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides
    @Singleton
    @AppScope
    fun appScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
