package com.gtkim.mobile_access_control.component.stats.domain.model

import com.gtkim.mobile_access_control.core.model.DenyReason

data class DenyReasonCount(
    val reason: DenyReason,
    val count: Int,
)
