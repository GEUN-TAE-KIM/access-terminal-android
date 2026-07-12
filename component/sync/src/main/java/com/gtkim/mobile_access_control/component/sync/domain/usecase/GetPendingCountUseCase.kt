package com.gtkim.mobile_access_control.component.sync.domain.usecase

import com.gtkim.mobile_access_control.component.sync.domain.repository.OfflineQueueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UI 의 "동기화 대기 N건" indicator 표시용.
 */
interface GetPendingCountUseCase {
    operator fun invoke(): Flow<Int>
}

internal class GetPendingCountUseCaseImpl @Inject constructor(
    private val repository: OfflineQueueRepository,
) : GetPendingCountUseCase {
    override operator fun invoke(): Flow<Int> = repository.pendingCount()
}
