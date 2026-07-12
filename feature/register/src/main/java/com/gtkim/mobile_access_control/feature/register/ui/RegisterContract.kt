package com.gtkim.mobile_access_control.feature.register.ui

import android.nfc.Tag
import com.gtkim.mobile_access_control.core.model.EmployeeCode
import com.gtkim.mobile_access_control.feature.common.ui.dialog.DialogState

internal data class RegisterUiState(
    /**
     * 사용자가 텍스트 입력한 대상 사번. 빈 값이면 스캔 버튼 비활성.
     *
     * master 캐시 셀렉터에서 텍스트 입력으로 전환된 이유는 PII 최소화 결정 (hybrid-offline.md §2.1 /
     * Phase 4 #14) — master 캐시에 user.name 을 저장하지 않으므로 셀렉터 표시에
     * 쓸 정보가 없다. 등록 시도 후 서버가 `404 USER_NOT_FOUND` / `422 USER_INACTIVE` 로 거부하면
     * 사용자에게 메시지를 보여준다.
     */
    val employeeCodeInput: String = "",
    val phase: Phase = Phase.IDLE,
    /** read 실패 / 등록 실패 / NFC 비활성화 등 사용자 노출 에러 다이얼로그. */
    val dialog: DialogState? = null,
) {
    /** 텍스트 입력값이 유효한 사번 형식이면 EmployeeCode 로 반환. 비어있거나 공백만이면 null. */
    val selectedEmployee: EmployeeCode?
        get() = employeeCodeInput.trim().takeIf { it.isNotEmpty() }?.let(::EmployeeCode)

    /**
     * IDLE        — 텍스트 입력 + 스캔 버튼. NFC reader mode 미활성.
     * SCANNING    — 사용자가 "카드 스캔" 누름. 카드 터치 대기. NFC reader mode 활성.
     * REGISTERING — 태그 감지됨. read + register 진행 중.
     * DONE        — 등록 완료. 확인 시 IDLE 로 복귀.
     */
    enum class Phase {
        IDLE,
        SCANNING,
        REGISTERING,
        DONE,
    }
}

internal sealed interface RegisterIntent {
    /** 텍스트 입력 변경. */
    data class EmployeeCodeChanged(
        val raw: String,
    ) : RegisterIntent

    /** "카드 스캔" 버튼 — SCANNING 진입, NFC reader mode 활성화. */
    data object StartScan : RegisterIntent

    /** 스캔 다이얼로그를 닫아 등록 플로우 취소 → IDLE 복귀. */
    data object CancelScan : RegisterIntent

    /** 실 NFC 진입점. enableReaderMode 콜백이 RouteScreen 에서 발사. */
    data class TagDetected(
        val tag: Tag,
    ) : RegisterIntent

    /** Activity 또는 NfcAdapter 가 null — 단말이 NFC 미지원. */
    data object NfcUnavailable : RegisterIntent

    /** NfcAdapter 는 있으나 isEnabled = false — 설정 화면으로 유도. */
    data object NfcDisabled : RegisterIntent

    /** 등록 완료 다이얼로그 확인 — IDLE 로 복귀. */
    data object ResultConfirmed : RegisterIntent

    data object DialogDismissed : RegisterIntent
    data object DialogConfirmed : RegisterIntent
}

internal sealed interface RegisterSideEffect {
    /** NFC 꺼짐 다이얼로그 확인 시 시스템 NFC 설정으로 유도. */
    data object OpenNfcSettings : RegisterSideEffect
}
