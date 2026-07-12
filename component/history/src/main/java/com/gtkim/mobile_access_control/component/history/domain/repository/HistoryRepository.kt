package com.gtkim.mobile_access_control.component.history.domain.repository

import com.gtkim.mobile_access_control.component.history.domain.model.HistoryError
import com.gtkim.mobile_access_control.component.history.domain.model.LogCursor
import com.gtkim.mobile_access_control.component.history.domain.model.LogFilter
import com.gtkim.mobile_access_control.component.history.domain.model.LogPage
import com.gtkim.mobile_access_control.core.common.result.Outcome

interface HistoryRepository {

    /**
     * 출입 기록 한 페이지를 조회한다 — `GET /api/v1/access/logs`.
     *
     * [cursor] 가 `null` 이면 첫 페이지. 이후 페이지는 직전 응답의 [LogPage.nextCursor] 를 넘긴다.
     */
    suspend fun logs(filter: LogFilter, cursor: LogCursor?): Outcome<LogPage, HistoryError>
}
