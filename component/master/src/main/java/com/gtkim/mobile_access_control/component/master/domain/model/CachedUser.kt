package com.gtkim.mobile_access_control.component.master.domain.model

import com.gtkim.mobile_access_control.core.model.EmployeeCode

/**
 * 캐시된 user. PII (name / department) 는 의도적으로 보관하지 않는다
 * (hybrid-offline.md §2.1).
 *
 * [id] 는 server PK — permission.userId join 에 사용. [employeeCode] 는 사용자 식별·표시용.
 */
data class CachedUser(
    val id: Long,
    val employeeCode: EmployeeCode,
    val isActive: Boolean,
)
