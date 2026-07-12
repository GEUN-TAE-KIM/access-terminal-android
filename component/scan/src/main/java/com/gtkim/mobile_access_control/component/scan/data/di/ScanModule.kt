package com.gtkim.mobile_access_control.component.scan.data.di

import com.gtkim.mobile_access_control.component.scan.domain.usecase.RegisterScannedCardUseCase
import com.gtkim.mobile_access_control.component.scan.domain.usecase.RegisterScannedCardUseCaseImpl
import com.gtkim.mobile_access_control.component.scan.domain.usecase.ScanCardUseCase
import com.gtkim.mobile_access_control.component.scan.domain.usecase.ScanCardUseCaseImpl
import com.gtkim.mobile_access_control.component.scan.domain.usecase.VerifyScannedCardUseCase
import com.gtkim.mobile_access_control.component.scan.domain.usecase.VerifyScannedCardUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ScanModule {

    @Binds
    abstract fun bindScanCardUseCase(impl: ScanCardUseCaseImpl): ScanCardUseCase

    @Binds
    abstract fun bindVerifyScannedCardUseCase(impl: VerifyScannedCardUseCaseImpl): VerifyScannedCardUseCase

    @Binds
    abstract fun bindRegisterScannedCardUseCase(impl: RegisterScannedCardUseCaseImpl): RegisterScannedCardUseCase
}
