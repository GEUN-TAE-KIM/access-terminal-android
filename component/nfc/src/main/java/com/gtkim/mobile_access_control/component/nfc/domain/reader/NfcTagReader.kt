package com.gtkim.mobile_access_control.component.nfc.domain.reader

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardData
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.core.common.result.Outcome

/**
 * Strategy 인터페이스 — 카드 종류별 구현 (FeliCa / IsoDep / Mock).
 * [supports] 가 true 인 Tag 에 대해서만 [read] 가 호출되어야 한다.
 *
 * NDEF 는 평문·인증 없음으로 사원증 검증 용도에 부적합하여 제외.
 */
interface NfcTagReader {
    val type: CardType
    fun supports(tag: Tag): Boolean
    suspend fun read(tag: Tag): Outcome<CardData, NfcError>
}
