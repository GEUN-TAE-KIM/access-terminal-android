package com.gtkim.mobile_access_control.component.history.domain.usecase

import com.gtkim.mobile_access_control.component.history.domain.model.HistoryError
import com.gtkim.mobile_access_control.component.history.domain.model.LogCursor
import com.gtkim.mobile_access_control.component.history.domain.model.LogFilter
import com.gtkim.mobile_access_control.component.history.domain.model.LogPage
import com.gtkim.mobile_access_control.component.history.domain.repository.HistoryRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import javax.inject.Inject

interface GetAccessLogsUseCase {
    suspend operator fun invoke(
        filter: LogFilter,
        cursor: LogCursor?
    ): Outcome<LogPage, HistoryError>
}

internal class GetAccessLogsUseCaseImpl @Inject constructor(
    private val repository: HistoryRepository,
) : GetAccessLogsUseCase {
    override suspend operator fun invoke(
        filter: LogFilter,
        cursor: LogCursor?,
    ): Outcome<LogPage, HistoryError> = repository.logs(filter, cursor)
}
