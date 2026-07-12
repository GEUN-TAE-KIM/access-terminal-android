package com.gtkim.mobile_access_control.component.stats.domain.usecase

import com.gtkim.mobile_access_control.component.stats.domain.model.DailyStats
import com.gtkim.mobile_access_control.component.stats.domain.model.StatsError
import com.gtkim.mobile_access_control.component.stats.domain.repository.StatsRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import java.time.LocalDate
import javax.inject.Inject

interface GetDailyStatsUseCase {
    suspend operator fun invoke(date: LocalDate): Outcome<DailyStats, StatsError>
}

internal class GetDailyStatsUseCaseImpl @Inject constructor(
    private val repository: StatsRepository,
) : GetDailyStatsUseCase {
    override suspend operator fun invoke(date: LocalDate): Outcome<DailyStats, StatsError> =
        repository.daily(date)
}
