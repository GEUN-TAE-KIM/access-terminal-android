package com.gtkim.mobile_access_control.di

import com.gtkim.mobile_access_control.core.di.qualifier.DefaultDispatcher
import com.gtkim.mobile_access_control.core.di.qualifier.IoDispatcher
import com.gtkim.mobile_access_control.core.di.qualifier.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides
    @DefaultDispatcher
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    @Provides
    @MainDispatcher
    fun mainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
