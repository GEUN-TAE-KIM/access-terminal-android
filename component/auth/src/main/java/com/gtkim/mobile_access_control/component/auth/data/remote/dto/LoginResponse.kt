package com.gtkim.mobile_access_control.component.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val accessTokenExpiresAt: String,
    val refreshTokenExpiresAt: String,
    val admin: AdminDto,
)
