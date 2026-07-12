package com.gtkim.mobile_access_control.component.scan.domain.usecase

import com.gtkim.mobile_access_control.component.access.domain.IdempotencyKeyGenerator
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.usecase.VerifyAccessUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType
import com.gtkim.mobile_access_control.component.scan.data.toAccessCardType
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import javax.inject.Inject

/**
 * Mock 시연 경로 — 실 [android.nfc.Tag] 없이 이미 가진 uid/cardType 으로 곧장 검증한다 (read 생략,
 * architecture.md §5 Mock 시연). [ScanCardUseCase] 와 검증 단계는 동일하되 NFC read 만 건너뛴다.
 *
 * cardType 은 `:component:nfc` 타입을 받아 내부에서 access 타입으로 매핑한다 (호출자가 매핑을
 * 떠안지 않게 함). 판정은 일반 검문과 동일 (online=서버 / offline=LocalAccessVerifier).
 */
interface VerifyScannedCardUseCase {
    suspend operator fun invoke(
        uid: CardUid,
        cardType: CardType,
        zone: Zone,
    ): Outcome<AccessResult, AppError>
}

internal class VerifyScannedCardUseCaseImpl @Inject constructor(
    private val verifyAccess: VerifyAccessUseCase,
    private val idempotencyKeyGenerator: IdempotencyKeyGenerator,
) : VerifyScannedCardUseCase {

    override suspend fun invoke(
        uid: CardUid,
        cardType: CardType,
        zone: Zone,
    ): Outcome<AccessResult, AppError> =
        verifyAccess(uid, cardType.toAccessCardType(), zone, idempotencyKeyGenerator.newKey())
}
