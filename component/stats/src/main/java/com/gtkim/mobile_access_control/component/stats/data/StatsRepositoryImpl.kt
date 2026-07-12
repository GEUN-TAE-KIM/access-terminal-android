package com.gtkim.mobile_access_control.component.stats.data

import com.gtkim.mobile_access_control.component.stats.data.error.toStatsError
import com.gtkim.mobile_access_control.component.stats.data.mapper.toDomain
import com.gtkim.mobile_access_control.component.stats.data.remote.StatsApi
import com.gtkim.mobile_access_control.component.stats.domain.model.DailyStats
import com.gtkim.mobile_access_control.component.stats.domain.model.StatsError
import com.gtkim.mobile_access_control.component.stats.domain.repository.StatsRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.result.safeCall
import java.time.LocalDate
import javax.inject.Inject

internal class StatsRepositoryImpl @Inject constructor(
    private val api: StatsApi,
) : StatsRepository {

    override suspend fun daily(date: LocalDate): Outcome<DailyStats, StatsError> =
        safeCall(Throwable::toStatsError) {
            // LocalDate.toString() == ISO YYYY-MM-DD.
            api.stats(date.toString()).toDomain()
        }
}
