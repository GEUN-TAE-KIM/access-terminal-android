package com.gtkim.mobile_access_control.component.sync.domain.usecase

import com.gtkim.mobile_access_control.component.sync.domain.repository.OfflineQueueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 개별 PendingLog 가 재시도 한도(10회, architecture.md §9)에 도달해 dead-letter 폐기된 이벤트를 노출.
 * UI 가 collect 해서 "네트워크/서버 상태 확인 필요" 안내를 띄운다.
 */
interface ObserveQueueDeadLetterUseCase {
    operator fun invoke(): Flow<Unit>
}

internal class ObserveQueueDeadLetterUseCaseImpl @Inject constructor(
    private val repository: OfflineQueueRepository,
) : ObserveQueueDeadLetterUseCase {
    override operator fun invoke(): Flow<Unit> = repository.deadLetterEvents
}
