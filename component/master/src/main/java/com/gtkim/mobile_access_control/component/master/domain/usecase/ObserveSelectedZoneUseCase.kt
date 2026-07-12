package com.gtkim.mobile_access_control.component.master.domain.usecase

import com.gtkim.mobile_access_control.component.master.domain.repository.TerminalSettings
import com.gtkim.mobile_access_control.core.model.Zone
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 현재 단말에 선택된 zone 을 관찰 (Phase 12).
 *
 * `null` 방출 → ScanViewModel 이 verify 진입 전에 picker 강제 노출 신호로 사용.
 * 운영자가 picker 에서 선택 시 [TerminalSettings.selectZone] 호출 → 본 Flow 즉시 반응.
 */
interface ObserveSelectedZoneUseCase {
    operator fun invoke(): Flow<Zone?>
}

internal class ObserveSelectedZoneUseCaseImpl @Inject constructor(
    private val settings: TerminalSettings,
) : ObserveSelectedZoneUseCase {
    override fun invoke(): Flow<Zone?> = settings.observeSelectedZone()
}
