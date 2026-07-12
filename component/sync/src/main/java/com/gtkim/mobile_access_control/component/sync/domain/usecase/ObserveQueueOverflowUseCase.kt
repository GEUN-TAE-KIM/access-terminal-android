package com.gtkim.mobile_access_control.component.sync.domain.usecase

import com.gtkim.mobile_access_control.component.sync.domain.repository.OfflineQueueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 큐가 한도(100건, architecture.md §9)를 초과해 가장 오래된 항목이 폐기된 이벤트를 노출.
 * UI 가 collect 해서 운영자에게 스낵바로 알린다.
 */
interface ObserveQueueOverflowUseCase {
    operator fun invoke(): Flow<Unit>
}

internal class ObserveQueueOverflowUseCaseImpl @Inject constructor(
    private val repository: OfflineQueueRepository,
) : ObserveQueueOverflowUseCase {
    override operator fun invoke(): Flow<Unit> = repository.overflowEvents
}
