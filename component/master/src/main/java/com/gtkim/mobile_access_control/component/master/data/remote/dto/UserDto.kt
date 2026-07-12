package com.gtkim.mobile_access_control.component.master.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * snapshot 응답의 user upserted 항목 (api-spec §8.1).
 *
 * `name` / `department` 는 wire 에는 오지만 mapper 단계에서 drop — Room 캐시에는 저장하지 않는다
 * (PII 최소화, hybrid-offline.md §2.1). 단말 분실 시 사원 명단 누출 방지.
 */
@Serializable
internal data class UserDto(
    val id: Long,
    val employeeCode: String,
    val name: String,
    val department: String? = null,
    val isActive: Boolean,
)
