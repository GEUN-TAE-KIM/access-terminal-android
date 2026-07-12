package com.gtkim.mobile_access_control.component.nfc.domain.service

import android.nfc.Tag
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardData
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.nfc.domain.reader.NfcTagReader
import com.gtkim.mobile_access_control.core.common.result.Outcome
import javax.inject.Inject

/**
 * 4종 Reader 의 Strategy 디스패처 — 통합 진입점.
 * Tag 가 들어오면 첫 supports() 매칭 Reader 에 위임한다.
 */
class NfcCardService @Inject constructor(
    private val readers: Set<@JvmSuppressWildcards NfcTagReader>,
) {
    suspend fun read(tag: Tag): Outcome<CardData, NfcError> {
        val reader = readers.firstOrNull { it.supports(tag) }
            ?: return Outcome.Failure(NfcError.UnreadableTag)
        return reader.read(tag)
    }
}
