package com.gtkim.mobile_access_control.core.common.time

import java.time.Instant
import java.time.ZoneId

/**
 * SUPREME RULE: 시각 비교는 반드시 이 인터페이스를 통해서만 수행한다.
 * System.currentTimeMillis(), Instant.now(), LocalDate.now() 직접 호출 금지.
 *
 * zoneId() 는 화면 표시용 (JST). 서버 통신은 항상 UTC 인 Instant 사용.
 */
interface TimeProvider {
    fun now(): Instant
    fun zoneId(): ZoneId
}
