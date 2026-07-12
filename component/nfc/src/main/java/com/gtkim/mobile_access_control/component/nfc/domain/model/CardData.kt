package com.gtkim.mobile_access_control.component.nfc.domain.model

import com.gtkim.mobile_access_control.core.model.CardUid

data class CardData(
    val uid: CardUid,
    val type: CardType,
    val payload: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardData) return false
        return uid == other.uid && type == other.type && payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
