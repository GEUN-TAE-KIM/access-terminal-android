package com.gtkim.mobile_access_control.component.history.domain.model

import com.gtkim.mobile_access_control.core.model.EmployeeCode

/** 출입 기록의 카드 등록 사용자 (API 명세 §5.1). 미등록 카드 기록이면 `null`. */
data class LogUser(
    val id: Long,
    val employeeCode: EmployeeCode,
    val name: String,
    val department: String?,
)
