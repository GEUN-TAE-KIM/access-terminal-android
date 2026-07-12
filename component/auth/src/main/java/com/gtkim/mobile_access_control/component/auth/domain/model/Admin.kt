package com.gtkim.mobile_access_control.component.auth.domain.model

data class Admin(
    val id: Long,
    val username: String,
    val name: String,
    val role: AdminRole,
)
