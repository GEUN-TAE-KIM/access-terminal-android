package com.gtkim.mobile_access_control.component.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequest(
    val username: String,
    val password: String,
)
