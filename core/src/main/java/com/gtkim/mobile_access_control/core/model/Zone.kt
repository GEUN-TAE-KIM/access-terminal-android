package com.gtkim.mobile_access_control.core.model

@JvmInline
value class Zone(val value: String) {
    init {
        require(value.isNotBlank()) { "Zone must not be blank" }
    }
}
