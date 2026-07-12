package com.gtkim.mobile_access_control.component.access.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class RegisterCardRequest(
    val cardUid: String,
    /** `FELICA` / `ISO_DEP` 만 허용 — `MOCK` 은 서버가 거부 (API 명세 §4.2). */
    val cardType: String,
    /** 카드를 매핑할 사용자의 사번 (비즈니스 식별자). DB PK 는 노출하지 않는다. */
    val employeeCode: String,
)
