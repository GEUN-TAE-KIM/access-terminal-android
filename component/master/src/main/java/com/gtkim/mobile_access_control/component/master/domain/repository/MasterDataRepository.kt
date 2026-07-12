package com.gtkim.mobile_access_control.component.master.domain.repository

import com.gtkim.mobile_access_control.component.master.domain.model.CachedCard
import com.gtkim.mobile_access_control.component.master.domain.model.CachedPermission
import com.gtkim.mobile_access_control.component.master.domain.model.CachedUser
import com.gtkim.mobile_access_control.component.master.domain.model.CachedZone
import com.gtkim.mobile_access_control.component.master.domain.model.MasterError
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import kotlinx.coroutines.flow.Flow

interface MasterDataRepository {

    /**
     * `GET /api/v1/master/snapshot` 호출 후 Room 캐시 갱신 (api-spec §8.1).
     *
     * 변경 없으면 noop (304). 변경 있으면 upserted/deletedIds 를 Room 에 batch 적용 + 새 ETag 저장.
     * 네트워크 / 서버 / 응답 형식 오류는 [MasterError] 로 매핑해 [Outcome.Failure] 로 반환하며,
     * 호출 측(Worker)이 재시도 정책을 결정한다.
     */
    suspend fun sync(): Outcome<Unit, MasterError>

    /**
     * 현재 캐시의 master snapshot ETag. audit log 의 `verifierVersion` 필드 (api-spec §5.2) source.
     *
     * 첫 sync 전이거나 캐시 손상 시 null — 그 시점엔 offline 판정도 불가능하다 (호출 측이 명세 검사).
     */
    suspend fun currentVersion(): String?

    suspend fun cardByUid(uid: CardUid): CachedCard?
    suspend fun userById(id: Long): CachedUser?
    suspend fun permissionsForUserAndZone(userId: Long, zone: Zone): List<CachedPermission>

    /**
     * 단말 설정 화면 picker 가 노출할 zone catalog (Phase 12).
     *
     * 비어있을 수 있다 — 첫 master sync 전이거나 서버에 zone 시드가 없는 경우. picker UI 는
     * 빈 경우 "master 동기화 대기 중" 안내 + 재시도 액션을 제공한다.
     */
    fun observeAvailableZones(): Flow<List<CachedZone>>
}
