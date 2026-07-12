package com.gtkim.mobile_access_control.component.sync.domain.repository

import com.gtkim.mobile_access_control.component.sync.domain.model.PendingLog
import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface OfflineQueueRepository {
    suspend fun enqueue(log: PendingLog)
    suspend fun pending(): List<PendingLog>
    fun pendingCount(): Flow<Int>
    suspend fun markUploaded(id: UUID)
    suspend fun markFailure(id: UUID)

    /**
     * 큐가 한도(`MAX_QUEUE_SIZE` = 100, architecture.md §9)를 초과해 가장 오래된 항목이 폐기됐을 때
     * 발사된다. UI 가 collect 해서 운영자에게 스낵바로 알린다.
     *
     * 본 interface 는 [Flow] 로 노출하지만 구현체는 hot (`MutableSharedFlow` 백킹) 이다 —
     * 일회성 이벤트라 cold replay 가 필요 없고, 테스트에서는 finite Flow (예: `emptyFlow`) 로
     * mock 하면 hot stream 의 무한 collect 가 orbit-test 종료 timeout 으로 잡히는 문제를 피할 수 있다.
     */
    val overflowEvents: Flow<Unit>

    /**
     * 개별 PendingLog 가 재시도 한도(`MAX_ATTEMPTS_PER_LOG` = 10) 에 도달해 dead-letter 폐기된
     * 시점에 발사된다. server `rejected` 응답이 없는데 네트워크 실패만 반복되는 비정상 케이스의
     * 안전망. UI 가 collect 해서 "네트워크/서버 상태 확인 필요" 안내를 띄운다.
     *
     * hot stream 백킹 / 테스트 mock 패턴은 [overflowEvents] 와 동일.
     */
    val deadLetterEvents: Flow<Unit>
}
