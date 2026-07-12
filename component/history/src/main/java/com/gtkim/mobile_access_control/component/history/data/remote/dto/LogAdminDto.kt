package com.gtkim.mobile_access_control.component.history.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LogAdminDto(
    val id: Long,
    val username: String,
    val name: String,
)
