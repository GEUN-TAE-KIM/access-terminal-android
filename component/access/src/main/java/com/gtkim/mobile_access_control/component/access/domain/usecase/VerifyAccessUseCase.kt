package com.gtkim.mobile_access_control.component.access.domain.usecase

import com.gtkim.mobile_access_control.component.access.domain.model.AccessError
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.model.CardType
import com.gtkim.mobile_access_control.component.access.domain.repository.AccessRepository
import com.gtkim.mobile_access_control.component.access.domain.service.LocalAccessVerifier
import com.gtkim.mobile_access_control.component.master.domain.repository.MasterDataRepository
import com.gtkim.mobile_access_control.component.sync.domain.model.PendingLog
import com.gtkim.mobile_access_control.component.sync.domain.provider.NetworkStateProvider
import com.gtkim.mobile_access_control.component.sync.domain.repository.OfflineQueueRepository
import com.gtkim.mobile_access_control.core.common.config.AppConfig
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * NFC 카드 검증 — Hybrid orchestrator (architecture.md §4 / hybrid-offline.md).
 *
 *  online  → [AccessRepository.verifyOnline] 호출. 서버가 결과 + access_logs INSERT 까지 처리.
 *  offline → [LocalAccessVerifier] 가 master 캐시로 자체 판정 + audit 큐잉 (`/access/logs/batch`).
 *
 * [idempotencyKey] 는 같은 검문 세션에서 재시도 시 동일 키를 유지한다 (architecture.md §2). 호출자
 * (검문 화면) 가 검문 세션 단위로 생성·보관해 넘긴다. offline 경로에서는 사용되지 않는다 (audit
 * batch 의 멱등 키는 `clientLogId`).
 */
interface VerifyAccessUseCase {
    suspend operator fun invoke(
        cardUid: CardUid,
        cardType: CardType,
        zone: Zone,
        idempotencyKey: String,
    ): Outcome<AccessResult, AccessError>
}

internal class VerifyAccessUseCaseImpl @Inject constructor(
    private val accessRepository: AccessRepository,
    private val localVerifier: LocalAccessVerifier,
    private val masterRepository: MasterDataRepository,
    private val offlineQueue: OfflineQueueRepository,
    private val networkState: NetworkStateProvider,
    private val appConfig: AppConfig,
) : VerifyAccessUseCase {

    override suspend operator fun invoke(
        cardUid: CardUid,
        cardType: CardType,
        zone: Zone,
        idempotencyKey: String,
    ): Outcome<AccessResult, AccessError> {
        if (networkState.isOnline()) {
            return accessRepository.verifyOnline(cardUid, cardType, zone, idempotencyKey)
        }
        return offlinePath(cardUid, cardType, zone)
    }

    private suspend fun offlinePath(
        cardUid: CardUid,
        cardType: CardType,
        zone: Zone,
    ): Outcome<AccessResult, AccessError> {
        val outcome = localVerifier.verify(cardUid, zone)
        // 미등록 카드 (CardNotRegistered) 는 audit 에 적재할 의미가 없다 — server access_logs 는
        // user_id NOT NULL 이라 멱등 키만 들고 가도 거부된다. 그대로 Failure 전파.
        if (outcome is Outcome.Success) {
            enqueueAudit(outcome.data, cardUid, cardType, zone)
        }
        return outcome
    }

    private suspend fun enqueueAudit(
        result: AccessResult,
        cardUid: CardUid,
        cardType: CardType,
        zone: Zone,
    ) {
        // master snapshot ETag 가 없으면 단말은 한 번도 sync 받은 적이 없다는 뜻 — 그런 단말은
        // offlinePath 자체가 도달하면 안 되지만, 방어적으로 audit 적재만 skip 하고 결과는 그대로 반환.
        val verifierVersion = masterRepository.currentVersion()
        if (verifierVersion == null) {
            Timber.w("Offline verify decided without master snapshot — skipping audit enqueue")
            return
        }
        offlineQueue.enqueue(
            PendingLog(
                id = UUID.randomUUID(),
                cardUid = cardUid,
                cardType = cardType.name,
                terminalId = appConfig.terminalId,
                zone = zone,
                decidedAt = result.verifiedAt,
                result = result.decision.name,
                denyReason = result.denyReason?.toWire(),
                verifierVersion = verifierVersion,
            ),
        )
    }
}
