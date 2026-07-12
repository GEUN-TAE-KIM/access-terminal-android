package com.gtkim.mobile_access_control.component.sync.domain.usecase

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.gtkim.mobile_access_control.component.sync.data.worker.OfflineFlushWorker
import com.gtkim.mobile_access_control.component.sync.domain.model.FlushState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 오프라인 큐 flush Worker (`OfflineFlushWorker.UNIQUE_NAME`) 의 진행 상태를 [FlushState] 로 노출.
 *
 * 자동 트리거 (네트워크 복구) 와 수동 트리거 (운영자 chip 탭) 모두 같은 unique work 라 본 Flow 는
 * 두 경로의 결과를 합쳐서 관찰한다. ScanViewModel 은 수동 트리거 직후의 결과만 SideEffect 로
 * 노출하고, 자동 트리거의 자체 결과는 조용히 보낸다 (잡음 방지).
 */
interface ObserveOfflineFlushStateUseCase {
    operator fun invoke(): Flow<FlushState>
}

internal class ObserveOfflineFlushStateUseCaseImpl @Inject constructor(
    private val workManager: WorkManager,
) : ObserveOfflineFlushStateUseCase {
    override fun invoke(): Flow<FlushState> = workManager
        .getWorkInfosForUniqueWorkFlow(OfflineFlushWorker.UNIQUE_NAME)
        .map { infos -> infos.lastOrNull()?.state.toFlushState() }
        .distinctUntilChanged()

    private fun WorkInfo.State?.toFlushState(): FlushState = when (this) {
        null, WorkInfo.State.CANCELLED -> FlushState.Idle
        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED, WorkInfo.State.RUNNING -> FlushState.Running
        WorkInfo.State.SUCCEEDED -> FlushState.Succeeded
        WorkInfo.State.FAILED -> FlushState.Failed
    }
}
