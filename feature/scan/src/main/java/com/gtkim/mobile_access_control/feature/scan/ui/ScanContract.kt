package com.gtkim.mobile_access_control.feature.scan.ui

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.master.domain.model.CachedZone
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import java.time.ZoneId

internal data class ScanUiState(
    /** validUntil 등 시각 표시용 타임존(JST). ViewModel 이 TimeProvider.zoneId() 로 1회 주입 — architecture.md §7. */
    val zoneId: ZoneId,
    val phase: Phase = Phase.IDLE,
    val lastResult: AccessResult? = null,
    /**
     * 스캔 플로우 도중 발생한 사용자 노출 가능 에러(read 실패, NFC 비활성화 등). DONE phase 에서
     * lastResult 가 null 이고 lastError 가 set 이면 다이얼로그가 에러 화면으로 표시된다.
     * IDLE 로 복귀할 때 함께 초기화된다.
     */
    val lastError: UiError? = null,
    /**
     * 검문 플로우 다이얼로그 외부에 떠야 하는 보조 다이얼로그. 검문 플로우 자체의 에러는
     * [lastError] 로 다룬다.
     */
    val dialog: DialogState? = null,
    val showMockPanel: Boolean = false,
    /**
     * 진행 중인 RESOLVING 이 Mock 스캔인지(실 NFC 미사용). Mock 은 NFC 하드웨어를 건드리면 안 되므로
     * RESOLVING 단계의 NFC reader mode 활성 판정에서 이 값으로 제외한다. RESOLVING 진입 시점에
     * 항상 새로 세팅된다 (실 NFC=false / Mock=true).
     */
    val mockScan: Boolean = false,
    /**
     * 본 단말이 배치된 zone (Phase 12). null = 첫 부팅 후 아직 미선택 — StartScan 시 picker 강제 노출.
     * verify 요청은 항상 이 값으로 보낸다
     */
    val selectedZone: Zone? = null,
    /**
     * master sync 로 캐시된 zone catalog (Phase 12). picker UI 의 옵션 리스트. 비어있으면 "동기화
     * 대기 중" 안내. (서버 V7 시드 기준 GATE-A / GATE-B 2종.)
     */
    val availableZones: List<CachedZone> = emptyList(),
    /** picker 다이얼로그 노출 여부. */
    val showZonePicker: Boolean = false,
) {
    /**
     * IDLE      — 버튼만 보임. NFC reader mode 미활성.
     * PROMPTING — 사용자가 "카드 스캔" 누름. 다이얼로그 열려 카드 터치 대기. NFC reader mode 활성.
     * RESOLVING — 태그 감지됨. read + verify 진행 중. NFC reader mode 유지.
     * DONE      — 결과 표시. 일정 시간 dwell 후 자동 IDLE 로 복귀.
     */
    enum class Phase {
        IDLE,
        PROMPTING,
        RESOLVING,
        DONE,
    }
}

internal sealed interface ScanIntent {
    /**
     * 사용자가 "카드 스캔" 버튼을 눌러 검문 플로우를 시작. 이전에는 화면 진입과 동시에 NFC 가
     * 활성화되어 의도하지 않은 스캔이 발생할 수 있었음 → 명시적 진입점으로 분리.
     */
    data object StartScan : ScanIntent

    /**
     * 사용자가 다이얼로그를 닫아 검문 플로우를 취소. dwell 도 함께 취소돼 즉시 IDLE 로 복귀.
     */
    data object CancelScan : ScanIntent

    /**
     * 실 NFC 진입점. enableReaderMode 콜백이 ScanRouteScreen 에서 직접 발사.
     */
    data class TagDetected(
        val tag: Tag,
    ) : ScanIntent

    /**
     * Debug 한정 Mock 패널 진입점. 실 NFC 의 TagDetected 와는 추상화 레벨이 다르므로
     * 의도적으로 별도 Intent 로 분리한다 (Mock 은 Tag 객체 자체를 만들 수 없어 read 단계를 건너뜀).
     *
     * zone 은 싣지 않는다 — 실 NFC([TagDetected]) 와 동일하게 ViewModel 이 [ScanUiState.selectedZone]
     * 단일 출처에서 읽는다. (검문 zone 은 단말 설정이지 카드 입력이 아니다.)
     *
     * @property cardType verify 요청에 실어 보낼 카드 타입. Mock 패널은 항상 [CardType.MOCK].
     */
    data class MockCardEmitted(
        val uid: CardUid,
        val cardType: CardType,
    ) : ScanIntent

    data class MockPanelToggled(
        val open: Boolean,
    ) : ScanIntent

    /**
     * Activity 또는 NfcAdapter 가 null — 단말이 NFC 를 물리적으로 미지원. 해결 불가.
     */
    data object NfcUnavailable : ScanIntent

    /**
     * NfcAdapter 는 있으나 isEnabled = false — 사용자가 설정에서 꺼둠. 설정 화면으로 유도.
     */
    data object NfcDisabled : ScanIntent

    data object DialogDismissed : ScanIntent
    data object DialogConfirmed : ScanIntent

    /** 사용자가 zone 변경 버튼을 눌러 picker 다이얼로그를 연다 (Phase 12). */
    data object OpenZonePicker : ScanIntent

    /** picker 다이얼로그를 닫는다. 선택 없이 dismiss 시 selectedZone 변경 없음. */
    data object DismissZonePicker : ScanIntent

    /** picker 옵션을 선택. 저장 + picker 닫기. */
    data class SelectZone(
        val zone: Zone,
    ) : ScanIntent
}

internal sealed interface ScanSideEffect {
    data object PlayGrantedSound : ScanSideEffect
    data object PlayDeniedSound : ScanSideEffect

    /**
     * 다이얼로그 확인 후 부수 액션. Context 가 필요해 ScanRouteScreen 의 SideEffect 핸들러에서 처리.
     */
    data object OpenNfcSettings : ScanSideEffect
}
