package com.gtkim.mobile_access_control.component.master.domain.usecase

import com.gtkim.mobile_access_control.component.master.domain.repository.TerminalSettings
import com.gtkim.mobile_access_control.core.model.Zone
import javax.inject.Inject

/**
 * picker 에서 선택된 zone 을 단말 설정에 저장 (Phase 12).
 *
 * 저장 즉시 [ObserveSelectedZoneUseCase] 가 새 값을 방출 → ScanViewModel 이 hardcoded 없이
 * 새 zone 으로 verify 를 보낸다.
 */
interface SaveSelectedZoneUseCase {
    suspend operator fun invoke(zone: Zone)
}

internal class SaveSelectedZoneUseCaseImpl @Inject constructor(
    private val settings: TerminalSettings,
) : SaveSelectedZoneUseCase {
    override suspend fun invoke(zone: Zone) = settings.selectZone(zone)
}
