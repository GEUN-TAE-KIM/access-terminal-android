package com.gtkim.mobile_access_control.core.model

@JvmInline
value class CardUid(val value: String) {
    init {
        require(value.isNotBlank()) { "CardUid must not be blank" }
    }
}
