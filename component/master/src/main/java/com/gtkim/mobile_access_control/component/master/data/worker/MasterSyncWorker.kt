package com.gtkim.mobile_access_control.component.master.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gtkim.mobile_access_control.component.master.domain.usecase.SyncMasterDataUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * 서버 snapshot 을 가져와 로컬 캐시(User/Card/Permission/Zone) 를 갱신.
 *
 * Hybrid 결정 (architecture.md §4) 이후 검문 단말의 staleness 책임을 sync 인프라가 짊어진다.
 * 트리거는 운영자가 검문 화면에서 "동기화" 를 명시적으로 요청한 경우 — [MasterSyncScheduler.syncNow]
 * → 본 워커. 로그인 직후 1회 sync 는 워커 대신 [SyncMasterDataUseCase] 를 LoginViewModel 이
 * 직접 await 한다 (검문 가용성 게이팅 의도).
 *
 * UseCase 가 도메인 결과를 [Outcome] 으로 반환하므로 try/catch 로 도메인 흐름을 제어하지 않는다 —
 * 진짜 버그성 throwable (NPE 등) 만 CoroutineWorker 의 기본 catch 로 전파돼 Worker 가 실패 처리.
 */
@HiltWorker
internal class MasterSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncMasterData: SyncMasterDataUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (val outcome = syncMasterData()) {
        is Outcome.Success -> Result.success()
        is Outcome.Failure -> {
            Timber.w("master sync failed (${outcome.error}) — will retry")
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME_ONCE = "master-sync-once"
    }
}
