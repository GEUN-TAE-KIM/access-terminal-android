package com.gtkim.mobile_access_control.component.scan.domain.usecase

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.access.domain.IdempotencyKeyGenerator
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.usecase.VerifyAccessUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.usecase.ReadNfcCardUseCase
import com.gtkim.mobile_access_control.component.scan.data.toAccessCardType
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.Zone
import javax.inject.Inject

/**
 * 검문 = NFC 카드 read → 출입 검증. cross-component orchestration (nfc read + access verify) 을
 * 도메인 레이어로 모은 application use case. ViewModel 은 결과(Outcome)만 받아 UI state 로 reduce 한다.
 *
 * 실패는 read 단계 [com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError] 또는 verify
 * 단계 [com.gtkim.mobile_access_control.component.access.domain.model.AccessError] 둘 다 가능하므로
 * 공통 상위 [AppError] 로 노출한다 (Outcome 의 E 가 `out` 분산이라 업캐스트 자동).
 *
 * Idempotency-Key 는 본 use case 가 1회 호출당 1개 발급한다 — 한 검문 = 한 키 (architecture.md §2). 같은
 * 검문의 네트워크 재시도는 verify 하위(interceptor)가 동일 키로 처리하므로 호출자는 키를 알 필요 없다.
 */
interface ScanCardUseCase {
    suspend operator fun invoke(tag: Tag, zone: Zone): Outcome<AccessResult, AppError>
}

internal class ScanCardUseCaseImpl @Inject constructor(
    private val readNfcCard: ReadNfcCardUseCase,
    private val verifyAccess: VerifyAccessUseCase,
    private val idempotencyKeyGenerator: IdempotencyKeyGenerator,
) : ScanCardUseCase {

    override suspend fun invoke(tag: Tag, zone: Zone): Outcome<AccessResult, AppError> =
        when (val card = readNfcCard(tag)) {
            is Outcome.Failure -> card
            is Outcome.Success -> verifyAccess(
                card.data.uid,
                card.data.type.toAccessCardType(),
                zone,
                idempotencyKeyGenerator.newKey(),
            )
        }
}
