package com.gtkim.mobile_access_control.feature.common.ui.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

/**
 * NFC Reader Mode lifecycle 을 호출 컴포저블 lifecycle 에 묶는 effect.
 *
 * 검문(`:feature:scan`)·카드 등록(`:feature:register`) 두 화면이 공유하므로 `:feature:common`
 * 에 둔다 — NFC 활성화 lifecycle 을 Compose `DisposableEffect` 가 직접 가지는 패턴 (architecture.md §4/§5).
 *
 * - [active] = false → 어떤 enableReaderMode 도 걸지 않고 가용성 콜백도 발사하지 않음.
 *   사용자가 스캔을 시작하기 전(IDLE) 의 기본 상태.
 * - [active] = true → 가용성 검사 후:
 *   - LocalActivity 또는 NfcAdapter 가 null → [onUnavailable] 발사. enableReaderMode 미설치.
 *   - Adapter 는 있으나 isEnabled = false → [onDisabled] 발사. enableReaderMode 미설치.
 *   - 정상 → [onTagDetected] 콜백으로 Tag 전달 (백그라운드 스레드).
 *
 * isEnabled 변화(앱 외부에서 NFC 토글)는 [NfcAdapter.ACTION_ADAPTER_STATE_CHANGED]
 * 브로드캐스트로 감지 → DisposableEffect key 재구성으로 enableReaderMode 재등록/해제.
 *
 * Flag 조합 (architecture.md §2/§5):
 *   NFC_A | NFC_B | NFC_F | NO_PLATFORM_SOUNDS | SKIP_NDEF_CHECK.
 */
@Composable
fun NfcReaderModeEffect(
    active: Boolean,
    onTagDetected: (Tag) -> Unit,
    onUnavailable: () -> Unit,
    onDisabled: () -> Unit,
) {
    val activity = LocalActivity.current
    val adapter = remember(activity) { activity?.let { NfcAdapter.getDefaultAdapter(it) } }

    var isEnabled by remember(adapter) { mutableStateOf(adapter?.isEnabled == true) }

    DisposableEffect(active, activity, adapter) {
        // 브로드캐스트 리시버는 active=true 일 때만 등록 — IDLE 상태에서 토글 추적 비용 절약.
        if (!active || activity == null || adapter == null) {
            return@DisposableEffect onDispose { }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                isEnabled = adapter.isEnabled
            }
        }
        ContextCompat.registerReceiver(
            activity,
            receiver,
            IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        isEnabled = adapter.isEnabled
        onDispose { activity.unregisterReceiver(receiver) }
    }

    LaunchedEffect(active, activity, adapter, isEnabled) {
        if (!active) return@LaunchedEffect
        when {
            activity == null || adapter == null -> onUnavailable()
            !isEnabled -> onDisabled()
            else -> Unit
        }
    }

    DisposableEffect(active, activity, adapter, isEnabled) {
        if (!active || activity == null || adapter == null || !isEnabled) {
            return@DisposableEffect onDispose { }
        }
        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
        adapter.enableReaderMode(
            activity,
            { tag -> onTagDetected(tag) },
            flags,
            null,
        )
        onDispose { adapter.disableReaderMode(activity) }
    }
}

/**
 * 스캔 진입 직전 버튼 onClick 에서 NFC 가용성을 동기로 분기하기 위한 Pre-flight helper.
 *
 * 이 함수만 두면 phase 전환 → reader mode effect → LaunchedEffect 가 onDisabled 발사 →
 * VM 핸들러 순서로 비동기 race 가 발생해 "카드를 대주세요" progress 다이얼로그가 한두 프레임
 * 깜빡인 뒤 "NFC 꺼짐" 다이얼로그로 바뀐다. onClick 시점에 adapter 를 직접 보고 분기하면
 * SCANNING phase 진입 자체가 안 일어나 깜빡임이 사라진다.
 *
 * `adapter` 인스턴스는 Activity 단위로 캐싱 (`remember`) 하되 `isEnabled` 는 매 호출마다
 * 동기로 읽는다 — IDLE 동안엔 [NfcReaderModeEffect] 의 broadcast 리시버가 등록되어 있지 않아
 * stale 상태일 수 있기 때문.
 *
 * @return `() -> Boolean` 호출자에게 enable 여부만 알리는 람다. null adapter 와 disabled 를
 * 각각 분기하고 싶으면 [NfcSystemState] 를 직접 받는 [rememberNfcSystemState] 를 쓰면 된다.
 */
@Composable
fun rememberNfcSystemState(): () -> NfcSystemState {
    val activity = LocalActivity.current
    val adapter = remember(activity) { activity?.let { NfcAdapter.getDefaultAdapter(it) } }
    return {
        when {
            activity == null || adapter == null -> NfcSystemState.Unavailable
            !adapter.isEnabled -> NfcSystemState.Disabled
            else -> NfcSystemState.Available
        }
    }
}

enum class NfcSystemState { Available, Unavailable, Disabled }

/**
 * "스캔 시작" 직전 NFC 가용성 게이트. [rememberNfcSystemState] 의 동기 검사 결과를 세 콜백으로 분기한다.
 *
 * scan·register RouteScreen 이 "스캔 시작" 인텐트를 가로채, SCANNING phase 진입 전에 NFC 가용성을
 * 확인하는 공통 패턴 — 비동기 [NfcReaderModeEffect] 의 onDisabled 발사보다 먼저 분기해 progress
 * 다이얼로그가 깜빡인 뒤 "NFC 꺼짐" 으로 바뀌는 race 를 막는다. 두 화면의 가용성→액션 매핑을 한 곳에 둔다.
 */
@Composable
fun rememberNfcStartGate(
    onUnavailable: () -> Unit,
    onDisabled: () -> Unit,
    onAvailable: () -> Unit,
): () -> Unit {
    val checkNfc = rememberNfcSystemState()
    return {
        when (checkNfc()) {
            NfcSystemState.Unavailable -> onUnavailable()
            NfcSystemState.Disabled -> onDisabled()
            NfcSystemState.Available -> onAvailable()
        }
    }
}
