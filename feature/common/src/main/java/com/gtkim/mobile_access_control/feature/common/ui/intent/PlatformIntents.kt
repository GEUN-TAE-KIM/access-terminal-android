package com.gtkim.mobile_access_control.feature.common.ui.intent

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * 화면별 RouteScreen 에서 SideEffect 처리 시 사용하는 시스템 Intent 헬퍼.
 *
 * 한 화면이 직접 [Context.startActivity] 호출해도 되지만, 같은 동작이 두 화면 이상에서 반복되면
 * 본 파일로 옮긴다 — Intent 액션 / URI 포맷 등 플랫폼 디테일이 한 곳에 모인다.
 */

/** 시스템 NFC 설정 화면 진입 (Disabled 경고에서 확인 시). */
fun Context.openNfcSettings() {
    startActivity(
        Intent(Settings.ACTION_NFC_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
