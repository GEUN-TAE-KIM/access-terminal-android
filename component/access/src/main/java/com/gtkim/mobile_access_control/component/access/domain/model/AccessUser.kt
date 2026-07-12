package com.gtkim.mobile_access_control.component.access.domain.model

import com.gtkim.mobile_access_control.core.model.EmployeeCode

/**
 * verify 응답의 카드 등록 사용자 정보 (API 명세 §4.1). 허가·거부 응답 모두 포함된다.
 */
data class AccessUser(
    val id: Long,
    val employeeCode: EmployeeCode,
    val name: String,
    val department: String?,
    /** 사용자 사진 URL. 운영 환경에서만 채워지며 본 시스템에서는 항상 `null` (API 명세 §4.1). */
    val photoUrl: String?,
)
