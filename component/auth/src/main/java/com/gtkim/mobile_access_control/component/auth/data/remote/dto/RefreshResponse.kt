package com.gtkim.mobile_access_control.component.auth.data.remote.dto

import kotlinx.serialization.Serializable

/** `/auth/refresh` 응답 — `/auth/login` 과 동일하되 `admin` 제외. */
@Serializable
internal data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val accessTokenExpiresAt: String,
    val refreshTokenExpiresAt: String,
)
