package com.gtkim.mobile_access_control.component.sync.domain.provider

import kotlinx.coroutines.flow.Flow

interface NetworkStateProvider {
    fun isOnline(): Boolean
    fun observe(): Flow<Boolean>
}
