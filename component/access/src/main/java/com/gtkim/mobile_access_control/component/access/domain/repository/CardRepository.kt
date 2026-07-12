package com.gtkim.mobile_access_control.component.access.domain.repository

import com.gtkim.mobile_access_control.component.access.domain.model.CardError
import com.gtkim.mobile_access_control.component.access.domain.model.CardType
import com.gtkim.mobile_access_control.component.access.domain.model.RegisteredCard
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode

interface CardRepository {

    /**
     * NFC 카드 등록 — `POST /api/v1/access/cards`. ADMIN role 한정 (서버 검증).
     *
     * 대상 사용자는 사번([EmployeeCode]) 으로 지정한다 — DB PK 미노출.
     * [cardType] 에 [CardType.MOCK] 을 넘기면 서버가 `400` 으로 거부한다 ([CardError.ValidationFailed]).
     */
    suspend fun register(
        cardUid: CardUid,
        cardType: CardType,
        employeeCode: EmployeeCode,
    ): Outcome<RegisteredCard, CardError>
}
