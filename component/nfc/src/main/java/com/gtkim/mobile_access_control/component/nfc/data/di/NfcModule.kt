package com.gtkim.mobile_access_control.component.nfc.data.di

import com.gtkim.mobile_access_control.component.nfc.data.reader.FeliCaReader
import com.gtkim.mobile_access_control.component.nfc.data.reader.IsoDepReader
import com.gtkim.mobile_access_control.component.nfc.domain.reader.NfcTagReader
import com.gtkim.mobile_access_control.component.nfc.domain.usecase.ReadNfcCardUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.usecase.ReadNfcCardUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Reader 멀티바인딩 + ReadNfcCardUseCase 바인딩.
 *
 * NDEF 는 평문 포맷이라 사원증 인증에 부적합하므로 의도적으로 제외.
 *
 * MockReader 는 이 Set 에 등록하지 않는다 (architecture.md §5) — 실 Tag 디스패치 경로에 끼면 진짜
 * 사원증이 Mock 으로 잘못 처리될 수 있다. Mock 시연은 :feature:scan 의 Debug 패널이 NfcCardService
 * 를 우회해 직접 verify 로 보내므로 multibinding 등록이 불필요하다.
 *
 * Reader Mode (NfcAdapter.enableReaderMode) 의 lifecycle 은 :feature:scan/ScanRouteScreen 의
 * DisposableEffect 가 직접 관리한다 — 별도 Controller/Broker 추상은 두지 않음.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NfcModule {
    @Binds
    @IntoSet
    abstract fun bindFeliCa(impl: FeliCaReader): NfcTagReader

    @Binds
    @IntoSet
    abstract fun bindIsoDep(impl: IsoDepReader): NfcTagReader

    @Binds
    abstract fun bindReadNfcCard(impl: ReadNfcCardUseCaseImpl): ReadNfcCardUseCase
}
