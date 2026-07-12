package com.gtkim.mobile_access_control.component.master.domain.repository

import com.gtkim.mobile_access_control.core.model.Zone
import kotlinx.coroutines.flow.Flow

/**
 * 단말 설정 저장소 (Phase 12 — zone 마스터화).
 *
 * 본 단말이 "어느 zone 에 배치되었는지" 를 영속한다. master sync 로 받은 zone catalog 중
 * 운영자가 picker 에서 선택한 [Zone] 의 code 를 저장한다. verify 요청은 본 값을 zone 필드로
 * 보낸다.
 *
 * null = 아직 선택되지 않음. 상단 ZoneIndicator 가 errorContainer 강조로 노출하고 시작 버튼이
 * disabled 된다 (architecture.md §17-A).
 *
 * 세션 정책: 로그아웃 시 [clearZone] 으로 영속값까지 제거 — 다음 로그인 시 미선택 디폴트.
 * Bootstrapper 가 [com.gtkim.mobile_access_control.component.auth.domain.model.AuthState] 의
 * LoggedIn → LoggedOut 전이를 관찰해 호출한다.
 */
interface TerminalSettings {
    /**
     * 현재 단말에 설정된 zone. picker 결과를 collect 해 UI 와 ViewModel 이 즉시 반응한다.
     */
    fun observeSelectedZone(): Flow<Zone?>

    suspend fun selectZone(zone: Zone)

    /**
     * 영속값까지 모두 제거. 로그아웃 시 호출 — 다음 로그인 시 미선택 디폴트로 재시작.
     */
    suspend fun clearZone()
}
