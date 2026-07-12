package com.gtkim.mobile_access_control.core.common.time

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Instant(UTC) → 화면 표시용 문자열 포맷터.
 *
 * 서버 통신은 UTC, 화면 표시는 단말 타임존 — 표시 zone 은 호출자가 [TimeProvider.zoneId] 로 넘긴다
 * (architecture.md §7). `ZoneId.systemDefault()` 등 직접 호출 금지.
 */
object AppDateTimeFormatter {
    private val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** 예: `2026-05-17 12:00:00` ([zone] 기준). */
    fun dateTime(instant: Instant, zone: ZoneId): String =
        DATE_TIME.format(instant.atZone(zone))

    /** 예: `2026-05-17` ([zone] 기준). */
    fun date(instant: Instant, zone: ZoneId): String =
        DATE.format(instant.atZone(zone))
}
