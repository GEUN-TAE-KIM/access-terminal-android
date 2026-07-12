package com.gtkim.mobile_access_control.component.master.data

import com.gtkim.mobile_access_control.component.master.data.error.toMasterError
import com.gtkim.mobile_access_control.component.master.data.mapper.toDomain
import com.gtkim.mobile_access_control.component.master.data.mapper.toEntity
import com.gtkim.mobile_access_control.component.master.data.remote.MasterDataApi
import com.gtkim.mobile_access_control.component.master.domain.model.CachedCard
import com.gtkim.mobile_access_control.component.master.domain.model.CachedPermission
import com.gtkim.mobile_access_control.component.master.domain.model.CachedUser
import com.gtkim.mobile_access_control.component.master.domain.model.CachedZone
import com.gtkim.mobile_access_control.component.master.domain.model.MasterError
import com.gtkim.mobile_access_control.component.master.domain.repository.MasterDataRepository
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.result.safeCall
import com.gtkim.mobile_access_control.core.database.master.dao.CardDao
import com.gtkim.mobile_access_control.core.database.master.dao.MasterDao
import com.gtkim.mobile_access_control.core.database.master.dao.PermissionDao
import com.gtkim.mobile_access_control.core.database.master.dao.UserDao
import com.gtkim.mobile_access_control.core.database.master.dao.ZoneDao
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class MasterDataRepositoryImpl @Inject constructor(
    private val api: MasterDataApi,
    private val masterDao: MasterDao,
    private val userDao: UserDao,
    private val cardDao: CardDao,
    private val permissionDao: PermissionDao,
    private val zoneDao: ZoneDao,
) : MasterDataRepository {

    override suspend fun sync(): Outcome<Unit, MasterError> {
        val previousETag = masterDao.currentETag()
        // 전송 계층 예외만 safeCall 로 봉인 — CancellationException 도 헬퍼가 rethrow 한다.
        // 200 OK + null body 같은 비즈니스 거부는 아래 status 분기에서 Outcome.Failure 로 표현.
        val response =
            when (val r = safeCall(Throwable::toMasterError) { api.snapshot(previousETag) }) {
                is Outcome.Failure -> return r
                is Outcome.Success -> r.data
            }

        return when (response.code()) {
            HTTP_NOT_MODIFIED -> {
                // 304 — 캐시 유지. 서버가 새 ETag 를 주면 갱신 (보통 같지만, weak/strong 변경 등 케이스).
                response.headers()["ETag"]?.let { masterDao.updateETag(it) }
                Outcome.Success(Unit)
            }

            HTTP_OK -> {
                val body = response.body()
                    ?: return Outcome.Failure(MasterError.EmptyBody)
                // body.version == ETag 값 (api-spec §8.1: "ETag 와 동일 값을 body 에도 둠").
                // Header 의 ETag 가 quote 로 감싸진 형태 (`"v1-..."`) 라 그대로 If-None-Match 에 echo 한다.
                val eTag = response.headers()["ETag"] ?: "\"${body.version}\""
                // ETag 를 캐시 row 와 같은 트랜잭션으로 적용 — 캐시만 갱신되고 ETag 가 따로 노는 desync 차단.
                masterDao.applySnapshot(
                    eTag = eTag,
                    usersUpserted = body.users.upserted.map { it.toEntity() },
                    usersDeletedIds = body.users.deletedIds,
                    cardsUpserted = body.cards.upserted.map { it.toEntity() },
                    cardsDeletedIds = body.cards.deletedIds,
                    permissionsUpserted = body.permissions.upserted.map { it.toEntity() },
                    permissionsDeletedIds = body.permissions.deletedIds,
                    zonesUpserted = body.zones.upserted.map { it.toEntity() },
                    zonesDeletedIds = body.zones.deletedIds,
                )
                Outcome.Success(Unit)
            }

            else -> Outcome.Failure(MasterError.UnexpectedStatus(response.code()))
        }
    }

    override suspend fun currentVersion(): String? = masterDao.currentETag()

    override suspend fun cardByUid(uid: CardUid): CachedCard? =
        cardDao.byUid(uid.value)?.toDomain()

    override suspend fun userById(id: Long): CachedUser? =
        userDao.byId(id)?.toDomain()

    override suspend fun permissionsForUserAndZone(
        userId: Long,
        zone: Zone
    ): List<CachedPermission> =
        permissionDao.byUserAndZone(userId, zone.value).map { it.toDomain() }

    override fun observeAvailableZones(): Flow<List<CachedZone>> =
        zoneDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    private companion object {
        const val HTTP_OK = 200
        const val HTTP_NOT_MODIFIED = 304
    }
}
