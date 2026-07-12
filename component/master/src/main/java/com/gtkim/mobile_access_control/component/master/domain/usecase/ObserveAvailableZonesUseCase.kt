package com.gtkim.mobile_access_control.component.master.domain.usecase

import com.gtkim.mobile_access_control.component.master.domain.model.CachedZone
import com.gtkim.mobile_access_control.component.master.domain.repository.MasterDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * master sync 로 캐시된 zone catalog 를 관찰 (Phase 12).
 *
 * 단말 설정 picker UI 가 본 Flow 를 collect 해 옵션 리스트를 노출. 빈 리스트면 "master 동기화
 * 대기 중" 안내 + 재시도 액션.
 */
interface ObserveAvailableZonesUseCase {
    operator fun invoke(): Flow<List<CachedZone>>
}

internal class ObserveAvailableZonesUseCaseImpl @Inject constructor(
    private val repository: MasterDataRepository,
) : ObserveAvailableZonesUseCase {
    override fun invoke(): Flow<List<CachedZone>> = repository.observeAvailableZones()
}
