package com.gtkim.mobile_access_control.component.sync.domain.usecase

import com.gtkim.mobile_access_control.component.sync.data.remote.AccessLogsBatchApi
import com.gtkim.mobile_access_control.component.sync.data.remote.dto.AccessLogItem
import com.gtkim.mobile_access_control.component.sync.data.remote.dto.AccessLogsBatchRequest
import com.gtkim.mobile_access_control.component.sync.domain.model.PendingLog
import com.gtkim.mobile_access_control.component.sync.domain.repository.OfflineQueueRepository
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject

/**
 * 오프라인 큐를 server batch upload 로 비운다 (`POST /api/v1/access/logs/batch`, api-spec §5.2).
 *
 * 응답의 accepted / duplicates / rejected 모두 큐에서 제거 — duplicates 는 이미 서버에 들어간
 * 멱등 hit, rejected 는 24h 초과 같은 영구 거부 (재전송해도 결과 같음, §5.2 정합). 네트워크 실패
 * / 5xx 같은 일시 실패는 [PendingLog.attempts] 만 올리고 다음 사이클 재시도.
 *
 * 100건 chunk — server 가 100건/요청 상한 (§5.2). 큐가 그보다 길면 여러 번 호출.
 */
interface FlushOfflineQueueUseCase {
    suspend operator fun invoke()
}

internal class FlushOfflineQueueUseCaseImpl @Inject constructor(
    private val repository: OfflineQueueRepository,
    private val api: AccessLogsBatchApi,
) : FlushOfflineQueueUseCase {

    override suspend operator fun invoke() {
        val all = repository.pending()
        if (all.isEmpty()) return

        all.chunked(BATCH_SIZE).forEach { batch ->
            uploadBatch(batch)
        }
    }

    private suspend fun uploadBatch(batch: List<PendingLog>) {
        val response = try {
            api.upload(AccessLogsBatchRequest(batch.map(::toWire)))
        } catch (t: Throwable) {
            // 코루틴 취소 신호는 비즈니스 실패가 아니라 lifecycle 신호 — markFailure 로 attempts 를
            // 부풀리지 않고 그대로 상위로 전파해 structured concurrency 정상 정리.
            if (t is CancellationException) throw t
            Timber.w(t, "Offline log batch upload failed — will retry next cycle")
            batch.forEach { repository.markFailure(it.id) }
            return
        }

        // accepted / duplicates 둘 다 서버 측에 row 가 존재 — 큐에서 제거.
        // 어떤 ID 가 accepted/duplicates 인지는 응답에 분리되어 있지 않지만, rejected 만 명시되므로
        // "batch 전체 ID 중 rejected 가 아닌 모든 ID" = 성공 처리 대상.
        val rejectedIds = response.rejected.map { it.clientLogId }.toSet()
        if (rejectedIds.isNotEmpty()) {
            Timber.w("Server rejected ${rejectedIds.size} audit log entries (e.g. > 24h)")
        }
        batch.forEach { log ->
            // rejected 도 영구 거부 (§5.2 — 재전송해도 결과 동일) 라 큐에서 제거.
            repository.markUploaded(log.id)
        }
        val disagreements = response.serverEvaluation.count { !it.agreement }
        if (disagreements > 0) {
            Timber.w("Drift detected: $disagreements / ${response.serverEvaluation.size} log entries disagree with server PermissionEvaluator")
        }
    }

    private fun toWire(log: PendingLog) = AccessLogItem(
        clientLogId = log.id.toString(),
        cardUid = log.cardUid.value,
        cardType = log.cardType,
        terminalId = log.terminalId,
        zone = log.zone.value,
        decidedAt = log.decidedAt.toString(),
        result = log.result,
        denyReason = log.denyReason,
        verifierVersion = log.verifierVersion,
    )

    private companion object {
        const val BATCH_SIZE = 100
    }
}
