package com.gtkim.mobile_access_control.component.access.data

import com.gtkim.mobile_access_control.component.access.domain.IdempotencyKeyGenerator
import java.util.UUID
import javax.inject.Inject

internal class IdempotencyKeyGeneratorImpl @Inject constructor() : IdempotencyKeyGenerator {
    override fun newKey(): String = UUID.randomUUID().toString()
}
