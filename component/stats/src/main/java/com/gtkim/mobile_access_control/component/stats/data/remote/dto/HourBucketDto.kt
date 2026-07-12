package com.gtkim.mobile_access_control.component.stats.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class HourBucketDto(
    val hour: Int,
    val allowed: Int,
    val denied: Int,
)
