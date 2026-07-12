package com.gtkim.mobile_access_control.component.auth.data.mapper

import com.gtkim.mobile_access_control.component.auth.data.remote.dto.AdminDto
import com.gtkim.mobile_access_control.component.auth.domain.model.Admin
import com.gtkim.mobile_access_control.component.auth.domain.model.AdminRole

internal fun AdminDto.toDomain(): Admin = Admin(
    id = id,
    username = username,
    name = name,
    role = AdminRole.fromWire(role),
)
