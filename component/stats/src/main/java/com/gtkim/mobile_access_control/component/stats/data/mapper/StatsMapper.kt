package com.gtkim.mobile_access_control.component.stats.data.mapper

import com.gtkim.mobile_access_control.component.stats.data.remote.dto.DailyStatsResponse
import com.gtkim.mobile_access_control.component.stats.data.remote.dto.DenyReasonBucketDto
import com.gtkim.mobile_access_control.component.stats.data.remote.dto.HourBucketDto
import com.gtkim.mobile_access_control.component.stats.data.remote.dto.SummaryDto
import com.gtkim.mobile_access_control.component.stats.domain.model.DailyStats
import com.gtkim.mobile_access_control.component.stats.domain.model.DenyReasonCount
import com.gtkim.mobile_access_control.component.stats.domain.model.HourlyCount
import com.gtkim.mobile_access_control.component.stats.domain.model.StatsSummary
import com.gtkim.mobile_access_control.core.model.DenyReason
import java.time.LocalDate

internal fun DailyStatsResponse.toDomain(): DailyStats = DailyStats(
    date = LocalDate.parse(date),
    summary = summary.toDomain(),
    byHour = byHour.map { it.toDomain() },
    byDenyReason = byDenyReason.map { it.toDomain() },
)

private fun SummaryDto.toDomain(): StatsSummary = StatsSummary(
    totalAttempts = totalAttempts,
    allowed = allowed,
    denied = denied,
    allowedRate = allowedRate,
)

private fun HourBucketDto.toDomain(): HourlyCount = HourlyCount(hour, allowed, denied)

private fun DenyReasonBucketDto.toDomain(): DenyReasonCount =
    DenyReasonCount(DenyReason.fromWire(reason), count)
