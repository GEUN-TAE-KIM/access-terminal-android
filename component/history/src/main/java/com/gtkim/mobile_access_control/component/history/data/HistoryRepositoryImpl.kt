package com.gtkim.mobile_access_control.component.history.data

import com.gtkim.mobile_access_control.component.history.data.error.toHistoryError
import com.gtkim.mobile_access_control.component.history.data.mapper.toDomain
import com.gtkim.mobile_access_control.component.history.data.remote.HistoryApi
import com.gtkim.mobile_access_control.component.history.domain.model.HistoryError
import com.gtkim.mobile_access_control.component.history.domain.model.LogCursor
import com.gtkim.mobile_access_control.component.history.domain.model.LogFilter
import com.gtkim.mobile_access_control.component.history.domain.model.LogPage
import com.gtkim.mobile_access_control.component.history.domain.model.LogResultFilter
import com.gtkim.mobile_access_control.component.history.domain.repository.HistoryRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.result.safeCall
import javax.inject.Inject

internal class HistoryRepositoryImpl @Inject constructor(
    private val api: HistoryApi,
) : HistoryRepository {

    override suspend fun logs(
        filter: LogFilter,
        cursor: LogCursor?,
    ): Outcome<LogPage, HistoryError> = safeCall(Throwable::toHistoryError) {
        api.logs(
            cursor = cursor?.value,
            size = filter.size,
            // Instant.toString() == ISO 8601 UTC datetime → 서버 tz 파라미터 불필요.
            from = filter.from?.toString(),
            to = filter.to?.toString(),
            // ALL 은 서버 기본값이므로 파라미터 자체를 생략한다.
            result = filter.result.takeIf { it != LogResultFilter.ALL }?.name,
            denyReason = filter.denyReason?.toWire(),
            employeeCode = filter.employeeCode?.value,
            cardUid = filter.cardUid?.value,
        ).toDomain()
    }
}
