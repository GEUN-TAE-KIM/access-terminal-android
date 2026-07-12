package com.gtkim.mobile_access_control.core.model

@JvmInline
value class EmployeeCode(val value: String) {
    init {
        require(value.isNotBlank()) { "EmployeeCode must not be blank" }
    }
}
