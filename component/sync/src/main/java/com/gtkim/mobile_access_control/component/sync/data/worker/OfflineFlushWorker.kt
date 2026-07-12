package com.gtkim.mobile_access_control.component.sync.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.gtkim.mobile_access_control.component.sync.domain.usecase.FlushOfflineQueueUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import timber.log.Timber

/**
 * 네트워크 복구 시 한 번, 또는 운영자 수동 트리거 시 큐를 비우는 워커.
 *
 * `flush()` 는 도메인 에러를 자체적으로 처리(batch 단위 `markFailure`)하므로 전체 함수가
 * Outcome 을 돌려주지는 않는다. Worker 단에서는 Room/IO 같은 진짜 throwable 만 retry 로
 * 받아내고, 코루틴 취소 신호(CancellationException) 는 rethrow 하여 WorkManager 의
 * cancel 흐름에 합류시킨다.
 *
 * **Retry 정책 — 1분 이내 결판** (2026-05-27):
 *   - [BackoffPolicy.LINEAR], 30초 간격 — exponential 의 무한 backoff 회피
 *   - [MAX_RUN_ATTEMPTS] = 2 (첫 시도 + 1회 retry). 그 이상은 [Result.failure] 로 종료
 *   - 한 사이클 ~30초 후 결판 → 사용자가 chip 누르고 빠르게 결과 알 수 있음
 *   - 못 보낸 큐는 다음 네트워크 변동 / 다음 수동 트리거 시 새 사이클에서 재시도되므로
 *     장기적으로는 영구 손실 없음
 */
@HiltWorker
class OfflineFlushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val flush: FlushOfflineQueueUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        flush()
        Result.success()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Timber.w(e, "offline flush failed (attempt ${runAttemptCount + 1}/$MAX_RUN_ATTEMPTS)")
        if (runAttemptCount + 1 >= MAX_RUN_ATTEMPTS) Result.failure() else Result.retry()
    }

    companion object {
        const val UNIQUE_NAME = "offline-flush"

        private const val MAX_RUN_ATTEMPTS = 2
        private const val BACKOFF_DELAY_SECONDS = 30L

        /**
         * 자동/수동 트리거가 같은 backoff 정책 + network constraint 를 갖도록 빌더를 단일 진입점으로 둔다.
         *
         * `NetworkType.CONNECTED` 안전망 — UI 단에서 isOnline 차단을 우회해 enqueue 가 들어와도
         * WorkManager 가 capability 검사로 deferred 처리 (`MasterSyncWorker` 와 동일 정책).
         * 이전엔 constraint 가 없어 NetworkStateProvider 는 offline 인데 localhost socket 만 통하는
         * 케이스에서 worker 가 그대로 실행돼 큐가 비워지는 inconsistency 가 있었다.
         */
        fun buildRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<OfflineFlushWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
                .build()
    }
}
