package com.gtkim.mobile_access_control.feature.scan.ui

import android.nfc.Tag
import androidx.lifecycle.viewModelScope
import com.gtkim.mobile_access_control.component.access.domain.model.AccessResult
import com.gtkim.mobile_access_control.component.auth.domain.usecase.LogoutUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.ObserveAvailableZonesUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.ObserveSelectedZoneUseCase
import com.gtkim.mobile_access_control.component.master.domain.usecase.SaveSelectedZoneUseCase
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.scan.domain.usecase.ScanCardUseCase
import com.gtkim.mobile_access_control.component.scan.domain.usecase.VerifyScannedCardUseCase
import com.gtkim.mobile_access_control.core.common.result.AppError
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.common.time.TimeProvider
import com.gtkim.mobile_access_control.core.model.CardUid
import com.gtkim.mobile_access_control.core.model.Zone
import com.gtkim.mobile_access_control.feature.common.ui.BaseViewModel
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState
import com.gtkim.mobile_access_control.feature.common.ui.error.UiError
import com.gtkim.mobile_access_control.feature.scan.mapper.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType as NfcCardType

@HiltViewModel
internal class ScanViewModel @Inject constructor(
    private val scanCardUseCase: ScanCardUseCase,
    private val verifyScannedCardUseCase: VerifyScannedCardUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeSelectedZoneUseCase: ObserveSelectedZoneUseCase,
    private val observeAvailableZonesUseCase: ObserveAvailableZonesUseCase,
    private val saveSelectedZoneUseCase: SaveSelectedZoneUseCase,
    private val timeProvider: TimeProvider,
) : BaseViewModel<ScanUiState, ScanSideEffect, ScanIntent>() {

    /**
     * DONE → IDLE 자동 복귀를 위한 dwell 타이머의 Job.
     */
    private var dwellJob: Job? = null

    @OptIn(OrbitExperimental::class)
    override val container: Container<ScanUiState, ScanSideEffect> =
        container(ScanUiState(zoneId = timeProvider.zoneId())) {
            repeatOnSubscription {
                launch {
                    observeSelectedZoneUseCase().collectLatest { zone ->
                        reduce { state.copy(selectedZone = zone) }
                    }
                }
                launch {
                    observeAvailableZonesUseCase().collectLatest { zones ->
                        reduce { state.copy(availableZones = zones) }
                    }
                }
            }
        }

    override fun onIntent(intent: ScanIntent) {
        when (intent) {
            is ScanIntent.StartScan -> startScan()
            is ScanIntent.CancelScan -> cancelScan()
            is ScanIntent.TagDetected -> handleTagDetected(intent.tag)
            is ScanIntent.MockCardEmitted -> handleMockCard(intent.uid, intent.cardType)
            is ScanIntent.MockPanelToggled -> toggleMockPanel(intent.open)
            is ScanIntent.NfcUnavailable -> showNfcSystemError(NfcError.NotSupported.toUiError())
            is ScanIntent.NfcDisabled -> showNfcSystemError(NfcError.Disabled.toUiError())
            is ScanIntent.DialogDismissed -> dismissDialog()
            is ScanIntent.DialogConfirmed -> confirmDialog()
            is ScanIntent.OpenZonePicker -> openZonePicker()
            is ScanIntent.DismissZonePicker -> dismissZonePicker()
            is ScanIntent.SelectZone -> selectZone(intent.zone)
        }
    }

    private fun startScan() = intent {
        if (state.phase != ScanUiState.Phase.IDLE || state.dialog != null) return@intent
        // selectedZone 가드는 UI 측에서 — ScanLandingPane 의 시작 버튼이 zone 미선택일 때 disabled
        // (Compose 가 즉시 disable 반영) + 미선택 시 picker 로 유도. ViewModel 에서 자동 picker 를
        // 열면 flow emission 타이밍 회귀 (StartScan 이 zone flow 보다 먼저 도달).
        reduce { state.copy(phase = ScanUiState.Phase.PROMPTING, mockScan = false) }
    }

    private fun cancelScan() = intent {
        if (state.phase == ScanUiState.Phase.IDLE) return@intent
        dwellJob?.cancel()
        dwellJob = null
        reduce { state.copy(phase = ScanUiState.Phase.IDLE, lastError = null) }
    }

    private fun handleTagDetected(tag: Tag) = intent {
        if (state.phase != ScanUiState.Phase.PROMPTING) return@intent
        // PROMPTING 진입은 startScan() 에서 selectedZone 비-null 을 확인한 뒤 — 여기에 도달했다면
        // selectedZone 은 non-null 이지만 방어적으로 한번 더 가드. 동시성으로 zone 이 사라졌다면
        // (운영자가 picker 로 clear 했을 경우 등) picker 재오픈.
        val zone = state.selectedZone ?: run {
            reduce { state.copy(phase = ScanUiState.Phase.IDLE, showZonePicker = true) }
            return@intent
        }

        reduce { state.copy(phase = ScanUiState.Phase.RESOLVING, dialog = null) }
        val resolveStart = TimeSource.Monotonic.markNow()
        val outcome = scanCardUseCase(tag, zone)
        awaitMinResolvingDuration(resolveStart)
        applyVerifyOutcome(outcome)
    }

    private fun handleMockCard(uid: CardUid, cardType: NfcCardType) = intent {
        if (state.phase == ScanUiState.Phase.RESOLVING) return@intent
        // zone 출처는 실 NFC(handleTagDetected) 와 동일하게 state.selectedZone 단일 — mock 버튼이
        // zone 미선택 시 disabled 라 도달 불가지만 방어적으로 가드 (동시성으로 zone 이 사라진 경우 picker 재오픈).
        val zone = state.selectedZone ?: run {
            reduce { state.copy(phase = ScanUiState.Phase.IDLE, showZonePicker = true) }
            return@intent
        }
        dwellJob?.cancel()
        dwellJob = null

        reduce {
            state.copy(
                phase = ScanUiState.Phase.RESOLVING,
                dialog = null,
                mockScan = true,
            )
        }
        val resolveStart = TimeSource.Monotonic.markNow()
        val outcome = verifyScannedCardUseCase(uid, cardType, zone)
        awaitMinResolvingDuration(resolveStart)
        applyVerifyOutcome(outcome)
    }

    // Mock / 로컬 캐시 경로는 verify 가 거의 즉시 끝나 RESOLVING 인디케이터가 1프레임 미만으로
    // 깜빡이는 loader-flash 가 발생. RESOLVING 진입 시각을 monotonic clock 으로 재서
    // 부족분만 채워 최소 표시 시간을 보장한다.
    private suspend fun awaitMinResolvingDuration(start: TimeMark) {
        val remaining = MIN_RESOLVING_DURATION - start.elapsedNow()
        if (remaining.isPositive()) delay(remaining)
    }

    private suspend fun Syntax<ScanUiState, ScanSideEffect>.applyVerifyOutcome(
        outcome: Outcome<AccessResult, AppError>,
    ) {
        when (outcome) {
            is Outcome.Failure -> reduce {
                state.copy(
                    phase = ScanUiState.Phase.DONE,
                    lastResult = null,
                    lastError = outcome.error.toUiError(),
                )
            }

            is Outcome.Success -> {
                val result = outcome.data
                reduce {
                    state.copy(
                        phase = ScanUiState.Phase.DONE,
                        lastResult = result,
                        lastError = null,
                    )
                }
                postSideEffect(
                    if (result.decision.isAllowed) ScanSideEffect.PlayGrantedSound
                    else ScanSideEffect.PlayDeniedSound,
                )
                scheduleDwellReset(forResult = result)
            }
        }
    }

    private fun scheduleDwellReset(forResult: AccessResult?) {
        dwellJob?.cancel()
        dwellJob = viewModelScope.launch {
            delay(RESULT_DWELL_MS)
            intent {
                val sameResult = forResult == null || state.lastResult == forResult
                if (state.phase == ScanUiState.Phase.DONE && sameResult) {
                    reduce { state.copy(phase = ScanUiState.Phase.IDLE, lastError = null) }
                }
            }
        }
    }

    private fun showNfcSystemError(uiError: UiError) = intent {
        dwellJob?.cancel()
        dwellJob = null
        reduce {
            state.copy(
                phase = ScanUiState.Phase.IDLE,
                lastResult = null,
                lastError = null,
                dialog = DialogState.Error(uiError),
            )
        }
    }

    private fun dismissDialog() = intent {
        if (state.dialog != null) reduce { state.copy(dialog = null) }
    }

    private fun confirmDialog() = intent {
        val systemAction = (state.dialog as? DialogState.Error)?.uiError?.action
        if (systemAction != null) {
            reduce { state.copy(dialog = null) }
            return@intent fireAction(systemAction)
        }
        val flowAction = state.lastError?.action ?: return@intent
        dwellJob?.cancel()
        dwellJob = null
        reduce { state.copy(phase = ScanUiState.Phase.IDLE, lastError = null) }
        fireAction(flowAction)
    }

    private suspend fun Syntax<ScanUiState, ScanSideEffect>.fireAction(action: UiError.Action) {
        when (action) {
            is UiError.Action.OpenNfcSettings -> postSideEffect(ScanSideEffect.OpenNfcSettings)
            is UiError.Action.Reauthenticate -> logoutUseCase()
            is UiError.Action.None -> Unit
        }
    }

    private fun toggleMockPanel(open: Boolean) = intent {
        reduce { state.copy(showMockPanel = open) }
    }

    private fun openZonePicker() = intent {
        if (state.phase != ScanUiState.Phase.IDLE) return@intent
        reduce { state.copy(showZonePicker = true) }
    }

    private fun dismissZonePicker() = intent {
        if (!state.showZonePicker) return@intent
        reduce { state.copy(showZonePicker = false) }
    }

    private fun selectZone(zone: Zone) = intent {
        saveSelectedZoneUseCase(zone)
        // Flow 가 새 값을 곧 방출하지만 picker 닫기는 즉시 — Flow 한 사이클 지연 회피.
        reduce { state.copy(showZonePicker = false, selectedZone = zone) }
    }

    companion object {
        // 결과 화면을 보여주는 시간. 끝나면 IDLE 로 복귀해 다음 스캔을 받는다.
        private const val RESULT_DWELL_MS = 2_000L

        // RESOLVING 인디케이터 최소 표시 시간 — Mock / 로컬 캐시 경로의 loader-flash 방지.
        private val MIN_RESOLVING_DURATION = 600.milliseconds
    }
}
