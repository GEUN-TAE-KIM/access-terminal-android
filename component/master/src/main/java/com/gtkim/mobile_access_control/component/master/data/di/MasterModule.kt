package com.gtkim.mobile_access_control.component.master.data.di

import com.gtkim.mobile_access_control.component.master.data.MasterDataRepositoryImpl
import com.gtkim.mobile_access_control.component.master.data.MasterSyncSchedulerImpl
import com.gtkim.mobile_access_control.component.master.data.local.TerminalSettingsImpl
import com.gtkim.mobile_access_control.component.master.data.remote.MasterDataApi
import com.gtkim.mobile_access_control.component.master.domain.MasterSyncScheduler
import com.gtkim.mobile_access_control.component.master.domain.repository.MasterDataRepository
import com.gtkim.mobile_access_control.component.master.domain.repository.TerminalSettings
import com.gtkim.mobile_access_control.component.master.domain.usecase.ObserveAvailableZonesUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.ObserveAvailableZonesUseCaseImpl
import com.gtkim.mobile_access_control.component.master.domain.usecase.ObserveSelectedZoneUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.ObserveSelectedZoneUseCaseImpl
import com.gtkim.mobile_access_control.component.master.domain.usecase.SaveSelectedZoneUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.SaveSelectedZoneUseCaseImpl
import com.gtkim.mobile_access_control.component.master.domain.usecase.SyncMasterDataUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.SyncMasterDataUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * UserDao / CardDao / PermissionDao / MasterDao 는 core.database.di.DatabaseModule 에서 @Provides 한다.
 * 이 모듈은 Repository / UseCase / Api 결선만 담당.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MasterModule {

    @Binds
    @Singleton
    abstract fun bindMasterRepository(impl: MasterDataRepositoryImpl): MasterDataRepository

    @Binds
    @Singleton
    abstract fun bindMasterSyncScheduler(impl: MasterSyncSchedulerImpl): MasterSyncScheduler

    @Binds
    @Singleton
    abstract fun bindTerminalSettings(impl: TerminalSettingsImpl): TerminalSettings

    @Binds
    abstract fun bindSyncMasterDataUseCase(impl: SyncMasterDataUseCaseImpl): SyncMasterDataUseCase

    @Binds
    abstract fun bindObserveAvailableZonesUseCase(
        impl: ObserveAvailableZonesUseCaseImpl,
    ): ObserveAvailableZonesUseCase

    @Binds
    abstract fun bindObserveSelectedZoneUseCase(
        impl: ObserveSelectedZoneUseCaseImpl,
    ): ObserveSelectedZoneUseCase

    @Binds
    abstract fun bindSaveSelectedZoneUseCase(
        impl: SaveSelectedZoneUseCaseImpl,
    ): SaveSelectedZoneUseCase

    companion object {
        @Provides
        @Singleton
        fun api(retrofit: Retrofit): MasterDataApi = retrofit.create(MasterDataApi::class.java)
    }
}
