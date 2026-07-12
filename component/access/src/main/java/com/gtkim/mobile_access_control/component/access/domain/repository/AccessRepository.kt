package com.gtkim.mobile_access_control.component.access.domain.repository

import com.gtkim.mobile_access_control.component.access.domain.model.AccessError
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.model.CardType
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone

interface AccessRepository {

    /**
     * 온라인 검증 — `POST /api/v1/access/verify`.
     *
     * `terminalId` / `timestamp` / `nonce` 는 구현체가 채운다 (각각 AppConfig / TimeProvider /
     * 매 호출 새 난수).
     *
     * [idempotencyKey] 는 같은 비즈니스 요청(같은 카드·zone 검문)에 동일 키를 유지한다 — 재시도마다
     * 새로 만들지 않는다 (architecture.md §2). 호출자(검문 화면)가 검문 세션 단위로 생성·보관한다.
     */
    suspend fun verifyOnline(
        cardUid: CardUid,
        cardType: CardType,
        zone: Zone,
        idempotencyKey: String,
    ): Outcome<AccessResult, AccessError>
}
