package com.gtkim.mobile_access_control.component.master.data

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gtkim.mobile_access_control.component.master.data.worker.MasterSyncWorker
import com.gtkim.mobile_access_control.component.master.domain.MasterSyncScheduler
import javax.inject.Inject

/**
 * WorkManager 기반 [MasterSyncScheduler] 구현.
 *
 * - 네트워크 제약(CONNECTED) 부여 — 오프라인이면 OS 가 복구까지 대기 후 실행
 * - ExistingWorkPolicy.KEEP — 이미 예약된 동기화가 있으면 중복 큐잉 방지
 *   (예: 운영자가 수동 동기화를 연타하거나, 진행 중에 화면 회전으로 호출이 다시 일어나는 경우 등)
 */
internal class MasterSyncSchedulerImpl @Inject constructor(
    private val workManager: WorkManager,
) : MasterSyncScheduler {

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun syncNow() {
        val request = OneTimeWorkRequestBuilder<MasterSyncWorker>()
            .setConstraints(networkConstraints)
            .build()
        workManager.enqueueUniqueWork(
            MasterSyncWorker.UNIQUE_NAME_ONCE,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
