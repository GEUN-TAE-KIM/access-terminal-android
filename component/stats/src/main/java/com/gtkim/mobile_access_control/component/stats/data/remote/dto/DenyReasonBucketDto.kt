package com.gtkim.mobile_access_control.component.stats.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class DenyReasonBucketDto(
    val reason: String,
    val count: Int,
)
