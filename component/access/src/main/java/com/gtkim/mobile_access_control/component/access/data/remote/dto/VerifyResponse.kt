package com.gtkim.mobile_access_control.component.access.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class VerifyResponse(
    val result: String,
    val logId: Long,
    val user: VerifyUserDto,
    val denyReason: String? = null,
    val validUntil: String? = null,
    val verifiedAt: String,
)
