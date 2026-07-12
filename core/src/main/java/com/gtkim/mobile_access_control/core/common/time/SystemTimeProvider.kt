package com.gtkim.mobile_access_control.core.common.time

import java.time.Instant
import java.time.ZoneId

class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Instant.now()
    override fun zoneId(): ZoneId = ZoneId.of("Asia/Tokyo")
}
