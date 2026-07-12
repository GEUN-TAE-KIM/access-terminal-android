package com.gtkim.mobile_access_control.di

import com.gtkim.mobile_access_control.core.common.config.AppConfig
import com.gtkim.mobile_access_control.logger.DebugTree
import com.gtkim.mobile_access_control.logger.ReleaseTree
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoggerModule {

    // 디버그 여부는 AppConfig 에서 받는다 — NetworkModule 과 동일 출처. 별도의 무자격(unqualified)
    // Boolean Hilt 바인딩을 두지 않아 그래프 충돌 여지를 없앤다.
    @Provides
    @Singleton
    fun timberTree(config: AppConfig): Timber.Tree =
        if (config.isDebug) DebugTree() else ReleaseTree()
}
