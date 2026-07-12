package com.gtkim.mobile_access_control.component.master.domain.model

import com.gtkim.mobile_access_control.core.model.Zone

/**
 * 캐시된 zone catalog 항목 (Phase 12 — zone 마스터화).
 *
 * 단말 설정 화면의 picker 가 본 모델 리스트를 노출하고, 운영자가 선택하면 그 [code] 를
 * SharedPrefs 에 저장한다. verify 요청 시 [Zone] 타입으로 변환해 사용.
 */
data class CachedZone(
    val id: Long,
    val code: Zone,
    val name: String,
)
