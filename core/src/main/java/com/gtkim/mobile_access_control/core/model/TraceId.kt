package com.gtkim.mobile_access_control.core.model

import java.util.UUID

@JvmInline
value class TraceId(val value: String) {
    companion object {
        fun new(): TraceId = TraceId(UUID.randomUUID().toString())
    }
}
