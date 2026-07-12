package com.gtkim.mobile_access_control.component.stats.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class DailyStatsResponse(
    val date: String,
    val summary: SummaryDto,
    val byHour: List<HourBucketDto>,
    val byDenyReason: List<DenyReasonBucketDto>,
)
