package com.gtkim.mobile_access_control.component.nfc.data.reader

import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.NfcF
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardData
import com.gtkim.mobile_access_control.component.nfc.domain.model.CardType
import com.gtkim.mobile_access_control.component.nfc.domain.model.NfcError
import com.gtkim.mobile_access_control.component.nfc.domain.reader.NfcTagReader
import com.gtkim.mobile_access_control.core.common.result.Outcome
import com.gtkim.mobile_access_control.core.model.CardUid
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

internal class FeliCaReader @Inject constructor() : NfcTagReader {
    override val type: CardType = CardType.FELICA
    override fun supports(tag: Tag): Boolean = NfcF.get(tag) != null

    override suspend fun read(tag: Tag): Outcome<CardData, NfcError> {
        val felica = NfcF.get(tag) ?: return Outcome.Failure(NfcError.UnreadableTag)
        return try {
            felica.connect()
            Outcome.Success(
                CardData(
                    uid = CardUid(tag.id.toHex()),
                    type = type,
                    payload = felica.manufacturer ?: ByteArray(0),
                ),
            )
        } catch (t: TagLostException) {
            Outcome.Failure(NfcError.TagLost)
        } catch (t: CancellationException) {
            // 코루틴 취소는 비즈니스 실패가 아니라 lifecycle 신호 — Io 로 둔갑시키지 않고 전파한다.
            throw t
        } catch (t: Throwable) {
            Outcome.Failure(NfcError.Io(t))
        } finally {
            runCatching { felica.close() }
        }
    }
}
