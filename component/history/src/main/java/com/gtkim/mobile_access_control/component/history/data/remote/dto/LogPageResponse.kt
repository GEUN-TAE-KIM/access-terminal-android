package com.gtkim.mobile_access_control.component.history.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LogPageResponse(
    val items: List<LogItemDto>,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)
