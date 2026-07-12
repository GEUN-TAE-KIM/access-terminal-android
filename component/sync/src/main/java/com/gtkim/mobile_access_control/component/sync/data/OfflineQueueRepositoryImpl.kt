package com.gtkim.mobile_access_control.component.sync.data

import com.gtkim.mobile_access_control.core.database.sync.dao.PendingLogDao
import com.gtkim.mobile_access_control.core.database.sync.entity.PendingLogEntity
import com.gtkim.mobile_access_control.component.sync.domain.model.PendingLog
import com.gtkim.mobile_access_control.component.sync.domain.repository.OfflineQueueRepository
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OfflineQueueRepositoryImpl @Inject constructor(
    private val dao: PendingLogDao,
) : OfflineQueueRepository {

    private val _overflowEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val overflowEvents: Flow<Unit> = _overflowEvents.asSharedFlow()

    private val _deadLetterEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val deadLetterEvents: Flow<Unit> = _deadLetterEvents.asSharedFlow()

    override suspend fun enqueue(log: PendingLog) {
        // architecture.md §9 — 최대 100건, 초과 시 가장 오래된 항목 폐기 + 사용자 알림. 신규 항목이 들어갈
        // 자리를 마련하기 위해 (>= MAX) 시점에 1건 폐기. 폭증으로 한 번에 여러 건 over 되는 케이스는
        // 매 enqueue 1건씩 정리되며 자연 수렴.
        val current = dao.countNow()
        if (current >= MAX_QUEUE_SIZE) {
            dao.deleteOldest(1)
            _overflowEvents.tryEmit(Unit)
            Timber.w("Offline queue overflow — discarded oldest entry (current=$current, max=$MAX_QUEUE_SIZE)")
        }
        dao.upsert(log.toEntity())
    }

    override suspend fun pending(): List<PendingLog> =
        dao.all().map { it.toDomain() }

    override fun pendingCount(): Flow<Int> = dao.count()

    override suspend fun markUploaded(id: UUID) {
        dao.deleteById(id.toString())
    }

    override suspend fun markFailure(id: UUID) {
        // attempts 증가 후 cap 체크. server 가 응답 못 보내고 네트워크 실패만 반복되는 비정상
        // 케이스에서 같은 항목이 무한 재시도 사이클을 도는 걸 차단한다. cap 도달 시 dead-letter
        // 폐기 + 운영자에게 안내 시그널.
        val attempts = dao.incrementAndGetAttempts(id.toString()) ?: return
        if (attempts >= MAX_ATTEMPTS_PER_LOG) {
            dao.deleteById(id.toString())
            _deadLetterEvents.tryEmit(Unit)
            Timber.w("PendingLog dead-letter — id=$id attempts=$attempts (cap=$MAX_ATTEMPTS_PER_LOG)")
        }
    }

    private fun PendingLog.toEntity() = PendingLogEntity(
        id = id.toString(),
        cardUid = cardUid.value,
        cardType = cardType,
        terminalId = terminalId,
        zone = zone.value,
        decidedAtEpochMs = decidedAt.toEpochMilli(),
        result = result,
        denyReason = denyReason,
        verifierVersion = verifierVersion,
        // 신규 audit 는 항상 0 회 시도로 적재. 재시도 카운트는 entity 컬럼이 단독 소유하고
        // markFailure 의 dao.incrementAndGetAttempts 로만 증가한다 (도메인 모델은 비보유).
        attempts = 0,
    )

    private fun PendingLogEntity.toDomain() = PendingLog(
        id = UUID.fromString(id),
        cardUid = CardUid(cardUid),
        cardType = cardType,
        terminalId = terminalId,
        zone = Zone(zone),
        decidedAt = Instant.ofEpochMilli(decidedAtEpochMs),
        result = result,
        denyReason = denyReason,
        verifierVersion = verifierVersion,
    )

    private companion object {
        const val MAX_QUEUE_SIZE = 100
        const val MAX_ATTEMPTS_PER_LOG = 10
    }
}
