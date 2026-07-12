package com.gtkim.mobile_access_control.component.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class AdminDto(
    val id: Long,
    val username: String,
    val name: String,
    val role: String,
)
