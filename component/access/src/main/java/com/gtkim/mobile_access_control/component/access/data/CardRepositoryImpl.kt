package com.gtkim.mobile_access_control.component.access.data

import com.gtkim.mobile_access_control.component.access.data.error.toCardError
import com.gtkim.mobile_access_control.component.access.data.mapper.toDomain
import com.gtkim.mobile_access_control.component.access.data.remote.AccessApi
import com.gtkim.mobile_access_control.component.access.data.remote.dto.RegisterCardRequest
import com.gtkim.mobile_access_control.component.access.domain.model.CardError
import com.gtkim.mobile_access_control.component.access.domain.model.CardType
import com.gtkim.mobile_access_control.component.access.domain.model.RegisteredCard
import com.gtkim.mobile_access_control.component.access.domain.repository.CardRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.result.safeCall
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import javax.inject.Inject

internal class CardRepositoryImpl @Inject constructor(
    private val api: AccessApi,
) : CardRepository {

    override suspend fun register(
        cardUid: CardUid,
        cardType: CardType,
        employeeCode: EmployeeCode,
    ): Outcome<RegisteredCard, CardError> = safeCall(Throwable::toCardError) {
        api.registerCard(
            RegisterCardRequest(
                cardUid = cardUid.value,
                cardType = cardType.name,
                employeeCode = employeeCode.value,
            ),
        ).toDomain()
    }
}
