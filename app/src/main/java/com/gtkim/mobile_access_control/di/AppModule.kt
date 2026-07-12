package com.gtkim.mobile_access_control.di

import android.content.Context
import androidx.work.WorkManager
import com.gtkim.mobile_access_control.BuildConfig
import com.gtkim.mobile_access_control.core.common.config.AppConfig
import com.gtkim.mobile_access_control.core.common.time.SystemTimeProvider
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun appConfig(): AppConfig = object : AppConfig {
        override val baseUrl: String = BuildConfig.BASE_URL
        override val isDebug: Boolean = BuildConfig.DEBUG
        override val terminalId: String = BuildConfig.TERMINAL_ID
    }

    @Provides
    @Singleton
    fun timeProvider(): TimeProvider = SystemTimeProvider()

    @Provides
    @Singleton
    fun workManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
