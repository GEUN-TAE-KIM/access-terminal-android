package com.gtkim.mobile_access_control.component.access.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class CardUserDto(
    val id: Long,
    val employeeCode: String,
    val name: String,
    val department: String? = null,
)
