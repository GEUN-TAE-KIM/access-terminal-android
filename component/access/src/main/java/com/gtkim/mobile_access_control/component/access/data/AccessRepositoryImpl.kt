package com.gtkim.mobile_access_control.component.access.data

import com.gtkim.mobile_access_control.component.access.data.error.toAccessError
import com.gtkim.mobile_access_control.component.access.data.mapper.toDomain
import com.gtkim.mobile_access_control.component.access.data.remote.AccessApi
import com.gtkim.mobile_access_control.component.access.data.remote.dto.VerifyRequest
import com.gtkim.mobile_access_control.component.access.domain.model.AccessError
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.model.CardType
import com.gtkim.mobile_access_control.component.access.domain.repository.AccessRepository
import com.gtkim.mobile_access_control.core.common.config.AppConfig
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.result.safeCall
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import java.security.SecureRandom
import javax.inject.Inject

internal class AccessRepositoryImpl @Inject constructor(
    private val api: AccessApi,
    private val appConfig: AppConfig,
    private val time: TimeProvider,
) : AccessRepository {

    private val secureRandom = SecureRandom()

    override suspend fun verifyOnline(
        cardUid: CardUid,
        cardType: CardType,
        zone: Zone,
        idempotencyKey: String,
    ): Outcome<AccessResult, AccessError> = safeCall(Throwable::toAccessError) {
        val request = VerifyRequest(
            cardUid = cardUid.value,
            cardType = cardType.name,
            terminalId = appConfig.terminalId,
            zone = zone.value,
            // Instant.toString() == ISO 8601 UTC. 직접 Instant.now() 금지 — TimeProvider 경유 (architecture.md §7).
            timestamp = time.now().toString(),
            nonce = newNonce(),
        )
        api.verify(idempotencyKey, request).toDomain()
    }

    /** 16바이트 난수 → hex 32자. 매 호출 새로 생성 (API 명세 §2 Replay 방어). */
    private fun newNonce(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
