package com.gtkim.mobile_access_control.component.access.domain.service

import com.gtkim.mobile_access_control.component.access.domain.model.AccessError
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone

/**
 * 오프라인 검증 — `:component:master` 캐시 (`/master/snapshot` 최신본) 만으로 즉시 판정.
 *
 * Hybrid 결정 (architecture.md §4) 의 핵심 컴포넌트. server `PermissionEvaluator` 와 같은 입력에
 * 같은 출력을 내야 한다 (golden test 매트릭스로 동치 검증, §19.3 / api-spec §8.1 raw 필드 정책).
 *
 * 평가 6단계 — 앞에서 컷되면 뒤는 평가하지 않음:
 *  1. card.isActive=false  → `Outcome.Success(AccessResult.DENIED_INACTIVE_CARD)`
 *  2. user.isActive=false  → `Outcome.Success(AccessResult.DENIED_INACTIVE_USER)`
 *  3. permissions(user, zone).isEmpty → `Outcome.Success(AccessResult.DENIED_NO_PERMISSION)`
 *  4. 모든 권한이 validUntil 통과 못 함 → `Outcome.Success(AccessResult.DENIED_EXPIRED)`
 *  5. 유효기간 통과한 권한 중 JST 허용 시간대 통과 없음 → `Outcome.Success(AccessResult.DENIED_OUT_OF_HOURS)`
 *  6. 위 통과 → `Outcome.Success(AccessResult.ALLOWED)` + 가장 관대한 validUntil
 *
 * 미등록 카드 (master 캐시에 UID 없음) 는 `Outcome.Failure(AccessError.CardNotRegistered)` —
 * "검증이 수행되지 않음" 의미. server 의 `404 ACCESS_CARD_NOT_REGISTERED` 와 정합.
 *
 * `validFrom` 은 wire snapshot 명세 (§8.1) 에 포함되지 않으므로 client 는 평가하지 않는다.
 * server 가 `is_active = TRUE` row 만 보내는 것으로 "이미 시작된 권한" 을 표현 (server
 * PermissionEvaluator 의 validFrom 검사도 같은 PR 묶음에서 제거).
 */
interface LocalAccessVerifier {
    suspend fun verify(cardUid: CardUid, zone: Zone): Outcome<AccessResult, AccessError>
}
