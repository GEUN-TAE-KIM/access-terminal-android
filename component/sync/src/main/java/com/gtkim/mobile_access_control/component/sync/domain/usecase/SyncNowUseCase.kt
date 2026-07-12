package com.gtkim.mobile_access_control.component.sync.domain.usecase

import com.gtkim.mobile_access_control.component.master.domain.MasterSyncScheduler
import javax.inject.Inject

/**
 * 운영자 수동 "지금 동기화" — master 데이터 갱신 + 미전송 audit 큐 즉시 전송을 한 액션으로 묶는다.
 * (AppShell TopBar 의 동기화 아이콘 → Confirm 다이얼로그 → 본 UseCase.)
 *
 * 두 작업 모두 WorkManager unique work + KEEP 정책이라 자동 트리거(네트워크 복구 시 Bootstrapper)와
 * 동시 발생해도 중복 enqueue 가 무시된다. fire-and-forget — 진행 결과는 [GetPendingCountUseCase] /
 * [ObserveOfflineFlushStateUseCase] 의 Flow 가 시각화한다. 온라인 가드는 호출자(AppShell)가 책임진다.
 *
 * 두 트리거를 묶는 비즈니스 동작을 ViewModel 밖으로 빼 ViewModel 이 [MasterSyncScheduler] 를 직접
 * 구동하지 않게 한다 (architecture.md §4 — ViewModel → UseCase 단방향).
 */
interface SyncNowUseCase {
    operator fun invoke()
}

internal class SyncNowUseCaseImpl @Inject constructor(
    private val requestOfflineFlush: RequestOfflineFlushUseCase,
    private val masterSyncScheduler: MasterSyncScheduler,
) : SyncNowUseCase {
    override operator fun invoke() {
        requestOfflineFlush()
        masterSyncScheduler.syncNow()
    }
}
