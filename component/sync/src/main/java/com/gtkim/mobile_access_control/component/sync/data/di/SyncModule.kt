package com.gtkim.mobile_access_control.component.sync.data.di

import com.gtkim.mobile_access_control.component.sync.data.OfflineQueueRepositoryImpl
import com.gtkim.mobile_access_control.component.sync.data.network.NetworkStateProviderImpl
import com.gtkim.mobile_access_control.component.sync.data.remote.AccessLogsBatchApi
import com.gtkim.mobile_access_control.component.sync.domain.provider.NetworkStateProvider
import com.gtkim.mobile_access_control.component.sync.domain.repository.OfflineQueueRepository
import com.gtkim.mobile_access_control.component.sync.domain.usecase.FlushOfflineQueueUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.FlushOfflineQueueUseCaseImpl
import com.gtkim.mobile_access_control.component.sync.domain.usecase.GetPendingCountUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.GetPendingCountUseCaseImpl
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveNetworkStateUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveNetworkStateUseCaseImpl
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveOfflineFlushStateUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveOfflineFlushStateUseCaseImpl
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveQueueDeadLetterUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveQueueDeadLetterUseCaseImpl
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveQueueOverflowUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.ObserveQueueOverflowUseCaseImpl
import com.gtkim.mobile_access_control.component.sync.domain.usecase.RequestOfflineFlushUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.RequestOfflineFlushUseCaseImpl
import com.gtkim.mobile_access_control.component.sync.domain.usecase.SyncNowUseCase
import com.gtkim.mobile_access_control.component.sync.domain.usecase.SyncNowUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * PendingLogDao 는 core.database.di.DatabaseModule 에서 @Provides 한다.
 * 이 모듈은 Repository / NetworkStateProvider / UseCase / Api 결선만 담당.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindQueueRepository(impl: OfflineQueueRepositoryImpl): OfflineQueueRepository

    @Binds
    @Singleton
    abstract fun bindNetworkStateProvider(impl: NetworkStateProviderImpl): NetworkStateProvider

    @Binds
    abstract fun bindFlushOfflineQueueUseCase(impl: FlushOfflineQueueUseCaseImpl): FlushOfflineQueueUseCase

    @Binds
    abstract fun bindGetPendingCountUseCase(impl: GetPendingCountUseCaseImpl): GetPendingCountUseCase

    @Binds
    abstract fun bindRequestOfflineFlushUseCase(impl: RequestOfflineFlushUseCaseImpl): RequestOfflineFlushUseCase

    @Binds
    abstract fun bindSyncNowUseCase(impl: SyncNowUseCaseImpl): SyncNowUseCase

    @Binds
    abstract fun bindObserveNetworkStateUseCase(impl: ObserveNetworkStateUseCaseImpl): ObserveNetworkStateUseCase

    @Binds
    abstract fun bindObserveOfflineFlushStateUseCase(impl: ObserveOfflineFlushStateUseCaseImpl): ObserveOfflineFlushStateUseCase

    @Binds
    abstract fun bindObserveQueueOverflowUseCase(impl: ObserveQueueOverflowUseCaseImpl): ObserveQueueOverflowUseCase

    @Binds
    abstract fun bindObserveQueueDeadLetterUseCase(impl: ObserveQueueDeadLetterUseCaseImpl): ObserveQueueDeadLetterUseCase

    companion object {
        @Provides
        @Singleton
        fun accessLogsBatchApi(retrofit: Retrofit): AccessLogsBatchApi =
            retrofit.create(AccessLogsBatchApi::class.java)
    }
}
