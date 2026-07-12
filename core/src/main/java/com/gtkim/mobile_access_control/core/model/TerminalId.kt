package com.gtkim.mobile_access_control.core.model

@JvmInline
value class TerminalId(val value: String) {
    init {
        require(value.isNotBlank()) { "TerminalId must not be blank" }
    }
}
