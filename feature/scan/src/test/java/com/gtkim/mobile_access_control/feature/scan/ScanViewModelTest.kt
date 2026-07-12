package com.gtkim.mobile_access_control.feature.scan

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.access.domain.model.AccessDecision
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.access.domain.model.AccessUser
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.ObserveAvailableZonesUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.ObserveSelectedZoneUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.SaveSelectedZoneUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.scan.domain.usecase.ScanCardUseCase
import com.gtkim.mobile_access_control.component.scan.domain.usecase.VerifyScannedCardUseCase
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.core.model.Zone
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.scan.ui.ScanIntent
import com.gtkim.mobile_access_control.feature.scan.ui.ScanSideEffect
import com.gtkim.mobile_access_control.feature.scan.ui.ScanUiState
import com.gtkim.mobile_access_control.feature.scan.ui.ScanViewModel
import com.gtkim.mobile_access_control.core.common.error.UiText
import com.gtkim.mobile_access_control.feature.common.R as CommonR
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeTimeProvider : TimeProvider {
        override fun now(): Instant = Instant.EPOCH
        override fun zoneId(): ZoneId = ZoneId.of("Asia/Tokyo")
    }

    private fun makeViewModel(
        scanCard: ScanCardUseCase = mockk(relaxed = true),
        verifyScanned: VerifyScannedCardUseCase = mockk(relaxed = true),
        logout: LogoutUseCase = mockk(relaxed = true),
        // Phase 12 — zone 마스터화. 기본 stub 은 GATE-A 가 이미 선택된 상태로 둬서 기존 테스트들이
        // "StartScan → PROMPTING" 가정을 그대로 유지하게 한다. zone 미선택 → picker 시나리오는
        // observeSelectedZone 를 override 해 테스트한다.
        observeSelectedZone: ObserveSelectedZoneUseCase = mockk<ObserveSelectedZoneUseCase>().also {
            every { it() } returns flowOf(Zone("GATE-A"))
        },
        observeAvailableZones: ObserveAvailableZonesUseCase = mockk<ObserveAvailableZonesUseCase>().also {
            every { it() } returns flowOf(emptyList())
        },
        saveSelectedZone: SaveSelectedZoneUseCase = mockk(relaxed = true),
        timeProvider: TimeProvider = FakeTimeProvider(),
    ): ScanViewModel = ScanViewModel(
        scanCard,
        verifyScanned,
        logout,
        observeSelectedZone,
        observeAvailableZones,
        saveSelectedZone,
        timeProvider,
    )

    // verify 응답은 cardUid 를 담지 않으므로(요청에만 존재) 결과 객체는 스캔 UID 와 무관 — 공유 가능.
    private val grantedResult = AccessResult(
        decision = AccessDecision.ALLOWED,
        logId = 1L,
        user = AccessUser(
            id = 1L,
            employeeCode = EmployeeCode("E-001"),
            name = "山田太郎",
            department = "建設部",
            photoUrl = null,
        ),
        denyReason = null,
        validUntil = null,
        verifiedAt = Instant.ofEpochSecond(0),
    )

    private val disabledUiError = UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_disabled),
        message = UiText.Res(CommonR.string.error_nfc_message_disabled),
        confirmText = UiText.Res(CommonR.string.error_button_open_settings),
        action = UiError.Action.OpenNfcSettings,
    )

    private val unsupportedUiError = UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_not_supported),
        message = UiText.Res(R.string.scan_error_nfc_not_supported_message),
        action = UiError.Action.None,
    )

    private val unreadableUiError = UiError(
        title = UiText.Res(CommonR.string.error_nfc_title_unreadable),
        message = UiText.Res(R.string.scan_error_nfc_unreadable_message),
    )

    // Phase 12 — observeSelectedZone() flow emission 으로 selectedZone 이 GATE-A 로 갱신되는 state
    // 전이를 각 테스트의 첫 expectState 에 흡수한다. 기본 mock 은 makeViewModel() 에서 GATE-A 를
    // 즉시 emit 하도록 설정.

    @Test
    fun `StartScan moves IDLE to PROMPTING`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            containerHost.onIntent(ScanIntent.StartScan)
            expectState { copy(phase = ScanUiState.Phase.PROMPTING) }
        }
    }

    @Test
    fun `StartScan is ignored when system dialog is open`() = runTest {
        // 시스템 다이얼로그가 떠있는 동안 StartScan 은 phase 가드로 무시 — 새 state 전이 없다.
        makeViewModel().test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            containerHost.onIntent(ScanIntent.NfcDisabled)
            expectState { copy(dialog = DialogState.Error(disabledUiError)) }
            // 다이얼로그 떠있는 상태에서 StartScan — 새 state 전이 없어야 한다.
            containerHost.onIntent(ScanIntent.StartScan)
        }
    }

    @Test
    fun `CancelScan returns to IDLE from PROMPTING`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            containerHost.onIntent(ScanIntent.StartScan)
            expectState { copy(phase = ScanUiState.Phase.PROMPTING) }
            containerHost.onIntent(ScanIntent.CancelScan)
            expectState { copy(phase = ScanUiState.Phase.IDLE) }
        }
    }

    @Test
    fun `TagDetected scans card and surfaces allowed result`() = runTest {
        val tag = mockk<Tag>()
        val zone = Zone("GATE-A")

        val scanCard = mockk<ScanCardUseCase>()
        coEvery { scanCard(tag, zone) } returns Outcome.Success(grantedResult)

        makeViewModel(scanCard = scanCard).test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            containerHost.onIntent(ScanIntent.StartScan)
            expectState { copy(phase = ScanUiState.Phase.PROMPTING) }
            containerHost.onIntent(ScanIntent.TagDetected(tag))
            expectState { copy(phase = ScanUiState.Phase.RESOLVING) }
            expectState {
                copy(
                    phase = ScanUiState.Phase.DONE,
                    lastResult = grantedResult,
                    lastError = null,
                )
            }
            expectSideEffect(ScanSideEffect.PlayGrantedSound)
            // RESULT_DWELL_MS 이후 IDLE 자동 복귀 (lastResult 는 유지)
            expectState { copy(phase = ScanUiState.Phase.IDLE, lastError = null) }
        }

        coVerify(exactly = 1) { scanCard(tag, zone) }
    }

    @Test
    fun `TagDetected with read failure sets lastError on state`() = runTest {
        val tag = mockk<Tag>()
        val zone = Zone("GATE-A")
        val scanCard = mockk<ScanCardUseCase>()
        coEvery { scanCard(tag, zone) } returns Outcome.Failure(NfcError.UnreadableTag)

        makeViewModel(scanCard = scanCard).test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            containerHost.onIntent(ScanIntent.StartScan)
            expectState { copy(phase = ScanUiState.Phase.PROMPTING) }
            containerHost.onIntent(ScanIntent.TagDetected(tag))
            expectState { copy(phase = ScanUiState.Phase.RESOLVING) }
            expectState {
                copy(
                    phase = ScanUiState.Phase.DONE,
                    lastResult = null,
                    lastError = unreadableUiError,
                )
            }
            // 에러는 dwell 없이 사용자 직접 닫을 때까지 다이얼로그 유지 — 추가 state 전이 없음.
        }

        coVerify(exactly = 1) { scanCard(tag, zone) }
    }

    @Test
    fun `TagDetected is dropped when phase is IDLE`() = runTest {
        val tag = mockk<Tag>()
        val scanCard = mockk<ScanCardUseCase>(relaxed = true)

        makeViewModel(scanCard = scanCard).test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            // PROMPTING 진입 없이 곧장 태그 — 방어 가드로 무시됨.
            containerHost.onIntent(ScanIntent.TagDetected(tag))
            // 어떤 state 전이도 없어야 한다.
        }

        coVerify(exactly = 0) { scanCard(any(), any()) }
    }

    @Test
    fun `MockCardEmitted verifies scanned card without NFC read`() = runTest {
        val uid = CardUid("MOCK-001")
        val zone = Zone("GATE-A")
        val scanCard = mockk<ScanCardUseCase>(relaxed = true)
        val verifyScanned = mockk<VerifyScannedCardUseCase>()
        coEvery { verifyScanned(uid, CardType.MOCK, zone) } returns Outcome.Success(grantedResult)

        makeViewModel(scanCard = scanCard, verifyScanned = verifyScanned).test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            // Mock 경로는 PROMPTING 단계를 건너뛸 수 있어야 한다 — 디버그 진입점.
            // zone 은 intent 가 아니라 state.selectedZone(GATE-A) 에서 읽힌다 (실 NFC 경로와 동일).
            containerHost.onIntent(ScanIntent.MockCardEmitted(uid, CardType.MOCK))
            expectState {
                copy(
                    phase = ScanUiState.Phase.RESOLVING,
                    mockScan = true,
                )
            }
            expectState {
                copy(
                    phase = ScanUiState.Phase.DONE,
                    lastResult = grantedResult,
                    lastError = null,
                )
            }
            expectSideEffect(ScanSideEffect.PlayGrantedSound)
            expectState { copy(phase = ScanUiState.Phase.IDLE, lastError = null) }
        }

        coVerify(exactly = 0) { scanCard(any(), any()) }
        coVerify(exactly = 1) { verifyScanned(uid, CardType.MOCK, zone) }
    }

    @Test
    fun `NfcUnavailable surfaces system error dialog and returns to IDLE`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            containerHost.onIntent(ScanIntent.StartScan)
            expectState { copy(phase = ScanUiState.Phase.PROMPTING) }
            containerHost.onIntent(ScanIntent.NfcUnavailable)
            // 시스템 에러는 검문 플로우 다이얼로그가 아닌 공통 [DialogState] 로 표면화 →
            // 카드 등록 화면과 같은 [AppDialog] 디자인을 공유.
            expectState {
                copy(
                    phase = ScanUiState.Phase.IDLE,
                    lastResult = null,
                    lastError = null,
                    dialog = DialogState.Error(unsupportedUiError),
                )
            }
        }
    }

    @Test
    fun `NfcDisabled surfaces system error dialog and returns to IDLE`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            containerHost.onIntent(ScanIntent.StartScan)
            expectState { copy(phase = ScanUiState.Phase.PROMPTING) }
            containerHost.onIntent(ScanIntent.NfcDisabled)
            expectState {
                copy(
                    phase = ScanUiState.Phase.IDLE,
                    lastResult = null,
                    lastError = null,
                    dialog = DialogState.Error(disabledUiError),
                )
            }
        }
    }

    @Test
    fun `NfcDisabled from IDLE (pre-flight path) also surfaces system error dialog`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            // Pre-flight NFC 가드 (ScanScreen) 가 StartScan 누르기 전에 직접 NfcDisabled 를 발사하는 경로.
            containerHost.onIntent(ScanIntent.NfcDisabled)
            expectState {
                copy(
                    phase = ScanUiState.Phase.IDLE,
                    dialog = DialogState.Error(disabledUiError),
                )
            }
        }
    }

    @Test
    fun `DialogConfirmed on Disabled fires OpenNfcSettings side effect and clears dialog`() = runTest {
        makeViewModel().test(this) {
            runOnCreate()
            expectState { copy(selectedZone = Zone("GATE-A")) }
            containerHost.onIntent(ScanIntent.NfcDisabled)
            expectState { copy(dialog = DialogState.Error(disabledUiError)) }
            containerHost.onIntent(ScanIntent.DialogConfirmed)
            expectState { copy(dialog = null) }
            expectSideEffect(ScanSideEffect.OpenNfcSettings)
        }
    }
}
