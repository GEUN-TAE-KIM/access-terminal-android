package com.gtkim.mobile_access_control.component.history.domain.model

/** 검문을 수행한 관리자 (API 명세 §5.1 응답 `items[].admin`). */
data class LogAdmin(
    val id: Long,
    val username: String,
    val name: String,
)
