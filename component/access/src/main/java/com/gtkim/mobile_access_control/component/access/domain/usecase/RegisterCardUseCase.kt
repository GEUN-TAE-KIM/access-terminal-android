package com.gtkim.mobile_access_control.component.access.domain.usecase

import com.gtkim.mobile_access_control.component.access.domain.model.CardError
import com.gtkim.mobile_access_control.component.access.domain.model.CardType
import com.gtkim.mobile_access_control.component.access.domain.model.RegisteredCard
import com.gtkim.mobile_access_control.component.access.domain.repository.CardRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import javax.inject.Inject

/**
 * NFC 카드 등록 UseCase — `POST /api/v1/access/cards`.
 *
 * 대상 사용자는 DB PK 가 아니라 사번([EmployeeCode]) 으로 지정한다 — `cardUid` 와 같은 비즈니스
 * 식별자 기조이며, 내부 PK 를 클라이언트 계약에 노출하지 않는다.
 *
 * ADMIN role 한정 작업이며, role 검증은 서버가 JWT 로 수행한다 ([CardError.ForbiddenRole]).
 */
interface RegisterCardUseCase {
    suspend operator fun invoke(
        cardUid: CardUid,
        cardType: CardType,
        employeeCode: EmployeeCode,
    ): Outcome<RegisteredCard, CardError>
}

internal class RegisterCardUseCaseImpl @Inject constructor(
    private val cardRepository: CardRepository,
) : RegisterCardUseCase {

    override suspend operator fun invoke(
        cardUid: CardUid,
        cardType: CardType,
        employeeCode: EmployeeCode,
    ): Outcome<RegisteredCard, CardError> = cardRepository.register(cardUid, cardType, employeeCode)
}
