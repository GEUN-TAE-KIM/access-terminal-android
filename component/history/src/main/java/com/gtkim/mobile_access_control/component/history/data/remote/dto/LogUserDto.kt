package com.gtkim.mobile_access_control.component.history.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LogUserDto(
    val id: Long,
    val employeeCode: String,
    val name: String,
    val department: String? = null,
)
