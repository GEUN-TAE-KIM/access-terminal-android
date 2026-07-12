package com.gtkim.mobile_access_control.component.history.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LogItemDto(
    val id: Long,
    val cardUid: String,
    val cardType: String,
    val result: String,
    val denyReason: String? = null,
    val user: LogUserDto? = null,
    val admin: LogAdminDto,
    val terminalId: String,
    val zone: String,
    val attemptedAt: String,
)
