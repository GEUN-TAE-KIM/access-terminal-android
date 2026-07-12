package com.gtkim.mobile_access_control.component.sync.domain.usecase

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.gtkim.mobile_access_control.component.sync.data.worker.OfflineFlushWorker
import javax.inject.Inject

/**
 * 운영자가 검문 화면에서 명시적으로 "미전송 큐 즉시 전송" 을 요청했을 때 호출.
 *
 * 자동 트리거 (네트워크 복구 시 Bootstrapper) 와 같은 [OfflineFlushWorker.UNIQUE_NAME] / KEEP 정책
 * 으로 enqueue 한다 — 이미 실행 중인 Worker 가 있으면 중복 enqueue 무시. ViewModel 은 fire-and-forget
 * 으로 즉시 다이얼로그를 닫고, 진행 결과는 [GetPendingCountUseCase] 의 Flow 가 감소하는 것으로 시각화.
 *
 * Worker 를 통해 가는 이유: 운영자가 화면을 이탈해도 백그라운드에서 끝까지 실행 + 실패 시 WorkManager
 * 의 exponential backoff retry. ViewModel scope 직접 호출은 화면 이탈 시 cancel 되어 부분 전송 후
 * 중단되는 케이스가 생긴다.
 */
interface RequestOfflineFlushUseCase {
    operator fun invoke()
}

internal class RequestOfflineFlushUseCaseImpl @Inject constructor(
    private val workManager: WorkManager,
) : RequestOfflineFlushUseCase {
    override operator fun invoke() {
        workManager.enqueueUniqueWork(
            OfflineFlushWorker.UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            OfflineFlushWorker.buildRequest(),
        )
    }
}
