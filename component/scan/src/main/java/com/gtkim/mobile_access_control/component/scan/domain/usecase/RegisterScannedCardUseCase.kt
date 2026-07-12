package com.gtkim.mobile_access_control.component.scan.domain.usecase

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.access.domain.model.RegisteredCard
import com.gtkim.mobile_access_control.component.access.domain.usecase.RegisterCardUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.usecase.ReadNfcCardUseCase
import com.gtkim.mobile_access_control.component.scan.data.toAccessCardType
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import javax.inject.Inject

/**
 * 카드 등록 = NFC 카드 read → 사번([EmployeeCode]) 에 매핑 등록. cross-component orchestration
 * (nfc read + access register) 을 도메인 레이어로 모은 application use case.
 *
 * 실패는 read 단계 [com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError] 또는 register
 * 단계 [com.gtkim.mobile_access_control.component.access.domain.model.CardError] 둘 다 가능하므로 공통
 * 상위 [AppError] 로 노출한다.
 */
interface RegisterScannedCardUseCase {
    suspend operator fun invoke(
        tag: Tag,
        employeeCode: EmployeeCode,
    ): Outcome<RegisteredCard, AppError>
}

internal class RegisterScannedCardUseCaseImpl @Inject constructor(
    private val readNfcCard: ReadNfcCardUseCase,
    private val registerCard: RegisterCardUseCase,
) : RegisterScannedCardUseCase {

    override suspend fun invoke(
        tag: Tag,
        employeeCode: EmployeeCode,
    ): Outcome<RegisteredCard, AppError> =
        when (val card = readNfcCard(tag)) {
            is Outcome.Failure -> card
            is Outcome.Success -> registerCard(
                card.data.uid,
                card.data.type.toAccessCardType(),
                employeeCode,
            )
        }
}
